# Adoptpet - CI/CD
Readme file at project directory shows how to build and deploy this project in different environments.

This document describes how to move to automatic builds and deployments.

During the course this project was developed for, several tools and platforms have been used.

For continuous integration stages, Tekton framework tasks were installed and run on a Kubernetes cluster.

# Continuous Integration
It is necessary to integrate changes to code as soon as possible, to validate that they are correct and
code can be compiled and tested and to be able to build all artifacts.

In order to achieve it, a pipeline should be created to perform a series of tasks to complete all steps.

## Steps
There are some activities to perform so cluster gets ready to start working and as part of the actual process.

### Prepare cluster
First step is to create an account with privileges to handle tasks and runners, get persistent volumes
and secrets or config maps available.

For these exercises, a cluster was set up and client configured so we could work on it,
[***namespaces***](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/) were also created
in advance. All this is beyond the scope of this document.

#### Secret
A [***secret***](https://kubernetes.io/docs/concepts/configuration/secret/) is an object that contains a small
amount of sensitive data such as a password, a token, or a key.

A secret for image registry (Docker in this case) is necessary so cluster can access it and pull/push images on behalf
of the user owner of that account.
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: dockerconfig-secret # this name is required for certain task, it will be used later
  namespace: <namespace-name>
stringData:
  config.json: | # this is the content of file config.json at ~/.docker and generated after logging in to docker.
    {
      "auths": {
        "<image-registry-url>": {
          "auth": "<token-for-image-registry>"
        }
      }
    }
```

#### Service account
A [***service account***](https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/) provides an identity for processes that run in a Pod, and maps to a ServiceAccount object.
There is always at least one ServiceAccount in each namespace.

A simple service account can be created applying against cluster this manifest
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
 name: <service-account-name> # this name will be used during role binding (see below)
 namespace: <namespace-name>
```
to enable it to use the secret previously created, these lines should be added at the end.
```yaml
imagePullSecrets:
  - name: dockerconfig-secret # this is the name assigned to secret  before
secrets:
  - name: dockerconfig-secret
```

#### Role
Role-based access control ([***RBAC***](https://kubernetes.io/docs/reference/access-authn-authz/rbac))
is a method of regulating access to computer or network resources.

A [***role***](https://kubernetes.io/docs/reference/access-authn-authz/rbac/#kubectl-create-role) contains rules that
represent a set of permissions, these are purely additive, there are no "deny" rules (whitelisting vs blacklisting).

A role always sets permissions within a particular namespace.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: <role-name> # this name will be used during role binding (see below)
  namespace: <namespace-name>
rules:
- apiGroups: [""] # role is allowed to access only api groups listed
  resources: ["pods", "persistentvolumeclaims", "secrets", "configmaps"] # role is allowed to access only resources added
  verbs: ["get", "list", "watch", "create", "update", "delete"] # role is allowed to execute only actions added
- apiGroups: ["tekton.dev"] # this means that api group, resource or action not listed is not allowed for this role
  resources: ["pipelineruns", "taskruns"]
  verbs: ["get", "list", "watch", "create", "update", "delete"]
```

Once role and service account are created, it is necessary to associate them so cluster knows that account is granted
those privileges.
[***Role binding***](https://kubernetes.io/docs/reference/access-authn-authz/rbac/#kubectl-create-rolebinding)
should be applied as listed below.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: <rolebinding-name> # name for this object (RoleBinding)
  namespace: <namespace-name>
subjects:
  - kind: ServiceAccount
    name: <service-account-name> # name created before
    namespace: <namespace-name>
roleRef:
  kind: Role
  name: <role-name> # name created before
  apiGroup: rbac.authorization.k8s.io
```

#### Persistent volume
A persistent volume ([***PV***](https://kubernetes.io/docs/concepts/storage/persistent-volumes/))
is a piece of storage in the cluster that has been provisioned by an administrator or dynamically.
A persistent volume claim (PVC) is a request for that storage by a user.

This process will require resources to store source code, compiled code and docker images, so a PVC should be created
so generated pods can use it.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: <workspace-name>
  namespace: <namespace-name>
spec:
  accessModes:
    - ReadWriteOnce # can be mounted as read-write by a single node
  resources:
    requests:
      storage: 1Gi # should be enough for this process requirements
```

#### Maven config map
A [***config map***](https://kubernetes.io/docs/concepts/configuration/configmap/)
is an object used to store non-confidential data in key-value pairs.
Pods can consume config maps as environment variables, command-line arguments, or as configuration files in a volume.

A config map allows to decouple environment-specific configuration from container images,
so that applications are easily portable.

This project is built and run using Apache Maven, maven task detailed later needs a particular config map to exist,
where value is the content of `settings.xml` file, empty sections are shown here, but in real projects different
servers (nexus, jfrog, etc.), proxies, profiles and so on, should be informed instead.

It can be generated using:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: maven-settings # this name is specific for maven task, so it has to be exactly the same
data:
  settings.xml: | 
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" 
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
      <pluginGroups>
        <!-- pluginGroup -->
      </pluginGroups>

      <proxies>
        <!-- proxy -->
      </proxies>

      <servers>
        <!-- server -->
      </servers>

      <mirrors>
        <!-- mirror -->
      </mirrors>

      <profiles>
        <!-- profile -->
      </profiles>
    </settings>
```

All these snippets have to be written into single `*.yaml` files and then applied against cluster (replacing names)

`kubectl apply -f <object-name>.yaml -n <namespace-name>`

output just shows object creation confirmation or error messages.

`<object-type>/<object-name> [created|configured|changed]`

last resource created should show this message

`<configmap>/<maven-settings> created`

From this point on focus goes to specific tasks related to project building.

### Clone Git repository
Next step is to retrieve code from version control repository, for current project this is [GitHub](https://github.com/).

[***`git-clone`***](https://hub.tekton.dev/tekton/task/git-clone) task must be available in the namespace the project
is being cloned into, there are different approaches for doing this, all executions used second one.

- Download task specification into a local YAML file and then apply into cluster.
  ```shell
    curl https://api.hub.tekton.dev/v1/resource/tekton/task/git-clone/0.9/raw > git-clone-task.yaml
    kubectl apply -f git-clone-task.yaml
  ```
- Apply using URL without saving it (suitable for most cases as, in general, there is no need to modify it).
  ```shell
    kubectl apply -f https://api.hub.tekton.dev/v1/resource/tekton/task/git-clone/0.9/raw
  ```
- Using Tekton CLI, only task name is needed.
  ```shell
    tkn hub install task git-clone
  ```
Several namespaces can coexist in a cluster, therefore, namespace has to be informed for every command,
to avoid doing it, this command can be run:
  ```shell
    kubectl config set-context --current --namespace=<namespace-name>
  ```
where _namespace-name_ is created for/by every user in the cluster.

Output should show this message:

`task.tekton.dev/git-clone configured`

After task is installed, it can be used by a task runner ([***`taskrun`***](https://tekton.dev/docs/pipelines/taskruns/))
or a pipeline, for now task runners will be used.

A new file must be created ***`git-clone-taskrun.yaml`***, with contents as listed below.

```yaml
apiVersion: tekton.dev/v1beta1 # indicates version of api to be used
kind: TaskRun # type of resource to be created, in this case a task runner
metadata:
  generateName: git-clone- # specifies prefix for resources created e.g. git-clone-xvf6j
  namespace: <namespace-name> # ensures right namespace is being used
spec:
  taskRef:
    kind: Task # indicates type of resource to be used
    name: git-clone # name of task to run, as indicated above
  podTemplate:
    securityContext:
      fsGroup: 65532 # ensures volumes are owned and writable by this GID 
  params:
  - name: url
    value: <repository-to-clone-url> # indicates url to clone code from, i.e. this repository
  - name: deleteExisting
    value: "true" # Clean out the contents of the destination directory if it already exists before cloning.
  workspaces:
    - name: output # location of destination repo, where code will be stored
      persistentVolumeClaim:
        claimName: workspace # volume used to mount workspace 
```
and applied to cluster.
  ```shell
    kubectl apply -f git-clone-taskrun.yaml
  ```
output for this command shows the name of task created, which is also prefix for pod's name.

`taskrun.tekton.dev/list-clone-d4rds created`

pods can be listed to check a new pod was created and running, also `-pod` suffix can be added to taskrun name.

logs can be now explored for this pod as well as output for execution.

  ```shell
    kubectl logs git-clone-d4rds-pod -n diploe2-ricp
  ```
output for this execution is shown

![Log for git clone task pod](../_resources/git-clone-output.png "Git Clone Output")

Following image shows information of commit retrieved: `3d46564a16708b7d92d0ee7f91f440747f2addde` and date: `1739640079`
which corresponds to **Feb 15, 2025 17:21:19** as well as repostitory cloned (this one).

![Repository, last commit hash and date](../_resources/git-clone-result.png "Git Clone Output")

#### Directory listing
It is not part of the process, but as a way of validation a [***`list-directory`***](
https://raw.githubusercontent.com/redhat-scholars/tekton-tutorial/refs/heads/master/workspaces/list-directory-task.yaml)
task can be run, another yaml (***`list-dir-taskrun.yaml`***) file should be created and applied against cluster.

```yaml
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  generateName: list-directory-ricp-
  namespace: <namespace-name>
spec:
  taskRef: # this is the task to be installed prior to execute this task runner, see above for details on how to install
    name: list-directory
  podTemplate:
    securityContext:
      fsGroup: 65532
  workspaces:
    - name: directory # workspace required by task, see Tekton Hub for specifications
      persistentVolumeClaim:
        claimName: workspace
```
  ```shell
    kubectl apply -f list-dir-taskrun.yaml
  ```
output for this command shows

`taskrun.tekton.dev/list-directory-ricp-xqr2m created`

and pod log should show files downloaded, only those at root directory, verifying all of them were properly downloaded
(date and size are correct).

  ```shell
    kubectl logs list-directory-ricp-xqr2m-pod -n diploe2-ricp
  ```

![List of files pulled from repository](../_resources/list-dir-output.png "List Directory Output")

### Build artifact
Once files are available within the cluster (in the persistent volume), it is possible to generate the artifact
(or binary) that will be used for image building and deployment.

This is a Java/Spring project built using Maven so it needs to be compiled and packaged into a runnable jar file.

To achieve this, [***`maven`***](https://hub.tekton.dev/tekton/task/maven) task needs to be installed

A new file must be created ***`build-app-taskrun.yaml`***, with contents as listed below.

```yaml
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  generateName: maven-ricp
  namespace: <namespace-name>
spec:
  podTemplate:
    securityContext:
      fsGroup: 65532
  taskRef:
    kind: Task
    name: maven
  params:
  - name: GOALS
    value: # list of goals and parameters to include in execution, if env vars need to be passed, should be added here
      - -B
      - -DskipTests
      - clean
      - package
  - name: MAVEN_IMAGE # maven image to build project, might need to change according to java version required by pom file
    value: gcr.io/cloud-builders/mvn@sha256:8f38a2667125a8d83f6e1997847fedb6a06f041c90e2244884153d85d95f869b
  workspaces:
  - name: maven-settings # workspace required by task to configure maven
    configmap:
      name: maven-settings # config map with settings for maven tool
  - name: source
    persistentVolumeClaim:
      claimName: workspace 
```
and applied to cluster.

  ```shell
    kubectl apply -f build-app-taskrun.yaml
  ```
output is, as before, name of task created

`taskrun.tekton.dev/maven-ricp-dzh5k created`

and by displaying logs output of mvn command is shown

  ```shell
    kubectl -n diploe2-ricp logs maven-ricp-dzh5k-pod -c step-mvn-goals
  ```

![Maven execution, downloading dependencies](../_resources/mvn-output.png "Maven task output")

all dependencies are downloaded, another workspace could be created to store local repository and avoid this process
in subsequent executions.

at the end, artifact generated and a BUILD SUCCESS message indicate that jar file is now available.

![Maven successful execution message](../_resources/mvn-success.png "Maven task output")

This jar file is executable directly using a suitable JRE, but it is not yet ready for a kubernetes deployment,
a Docker image will be created and pushed to Docker Hub so it is available to deploy to the cluster.

### Build and push Docker Image
It is time to pack this artifact as a container image, this is achieved through Dockerfile at top directory of this
project, and push it to image registry, DockerHub in this case.

Another task, [***`buildha`***](https://hub.tekton.dev/tekton/task/buildah), has to be installed and a new task run
created ***`build-image-taskrun.yaml`***, with contents as listed below.

```yaml
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  generateName: buildah-ricp-
  namespace: <namespace-name>
spec:
  taskRef:
    name: buildah
  params:
    - name: IMAGE
      value: 'docker.io/sacnavi/adoptpetsvc:v8' # this could be different depending on versioning strategy
    - name: TLSVERIFY # Verify the TLS on the registry endpoint (for push/pull to a non-TLS registry)
      value: 'false'
    - name: STORAGE_DRIVER # Set buildah storage driver
      value: 'vfs' # good for testing/debbuging purposes, for production environments overlay2 would be a better option
  workspaces:
    - name: source
      persistentVolumeClaim:
        claimName: workspace-ricp
    - name: dockerconfig # this is required by buildha task to access image registry
      secret:
        secretName: dockerconfig-secret # this is the secret configured at first steps above
```
and applied to cluster.

  ```shell
    kubectl apply -f build-image-taskrun.yaml
  ```
output is, once again, name of task created

`taskrun.tekton.dev/buildah-ricp-dvdcd created`

reviewing logs:

  ```shell
    kubectl logs buildah-ricp-dvdcd-pod 
  ```

![Docker image build process](../_resources/buildha-output.png "Buildha output")

image created and tagged, digest `182af8ef75fa3d2cb4fcfcd097e460dbc205d7d870dcc68b64cc3826b6b7fe79` generated,
and pushed to registry

![Image successfully built, tagged and pushed](../_resources/buildha-success.png "Buildha output")

this information can be verified on DockerHub site at [sacnavi/adoptpetsvc:v8](
https://hub.docker.com/layers/sacnavi/adoptpetsvc/v8/images/sha256-182af8ef75fa3d2cb4fcfcd097e460dbc205d7d870dcc68b64cc3826b6b7fe79)

![DockerHub updated with new tag](../_resources/buildha-registry.png "DockerHub tag")

# Continuous Deployment
As discussed, it is important to integrate changes and build artifacts periodically, but it is also to make them
available so they can be used and tested in actual environments.

### Application deployment
Deploying the app requires to instantiate applications and services (such as databases) as containers in the cluster.

For this step, a new task ([***`kubernetes-actions`***](https://hub.tekton.dev/tekton/task/kubernetes-actions))
is needed to perform a set of commands as shown below.

```yaml
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  generateName: kubernetes-actions-
  namespace: <namespace-name>
spec:
  serviceAccountName: tekton-sa
  taskRef:
    name: kubernetes-actions
  params:
    - name: script
      value: | # this is the set of commands to run by task
        kubectl delete deployment adoptpetsvc # removes previously deployed version 
        kubectl create deployment adoptpetsvc --image=<full-image-url> # creates new version using specified version  
        kubectl set env --from=configmap/adoptpetsvc-cm deployment/adoptpetsvc # add necessary environment variable from
        # a config map, in this case it is the url of the database service the app will run against
        echo "----------" # displays a message
        kubectl get deployment # shows all active deployments.
  workspaces:
    - name: kubeconfig-dir # stores a config file that allows to use a different cluster for these commands
      emptyDir: {} # as it is not used, an empty directory is defined here
    - name: manifest-dir # same for manifest files 
      emptyDir: {} # again it is not used, so empty directory defined
```

By exploring logs for pod created, output should look like next image.
```text
Defaulted container "step-kubectl" out of: step-kubectl, prepare (init), place-scripts (init)
deployment.apps "adoptpetsvc" deleted
deployment.apps/adoptpetsvc created
deployment.apps/adoptpetsvc env updated
----------
NAME                     READY   UP-TO-DATE   AVAILABLE   AGE
el-event-listener-cicd   1/1     1            1           4d11h
adoptpetsvc              0/1     1            0           0s

```

The goal now is to perform all these tasks as a single process that can be run automatically (it can be also a manual
process) so every change is included immediately after pushed to repository,

### Pipeline
After all previous steps have completed, they can be integrated in a [***`Pipeline`***](
https://tekton.dev/docs/pipelines/pipelines/) so they can be run as a single process either using a task runner or a trigger.

Pipeline definition contains the list of tasks to perform and information (parameters and workspaces) they require.

```yaml
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: pipeline-cicd # name that will be used by task runners or event listeners
  namespace: <namespace-name>
spec:
  description: | # detailed description of the pipeline.
    This pipeline runs full CI/CD cycle from fetching repository code, compile and package,
    containerization, push to image registry and deployment to kubernetes cluster.
  params:
    - name: repo-url # this is required by git-clone task
      type: string
    - name: maven-image # required by maven task
      type: string
    - name: container-image # required by buildah task to indicate image name and tag to generate and push
      type: string
    - name: deployment-name # name of the object use to deploy, it is used to erase and create deployments
      type: string
  workspaces:
    - name: workspace # this is the general storage, will contain source code, dependencies, compiled code and image
      # is used by all tasks that require storage (all tasks in this pipeline)
    - name: maven-settings # this contains maven settings (servers, proxies, profiles, etc.)
    - name: dockerconfig # this is used by buildah task to retrieve docker credentials
  tasks: # list of tasks that pipeline will execute
    - name: empty-dir # for some tasks there is no need to store/retrieve information so empty directories are declared
```

Tasks used are the same already detailed above, there are a couple of changes made for pipeline:

- Since now they are within another process, it is necessary to establish execution order. This is achieved by using
  a new field in task specification ***`runAfter`*** to indicate not only precedence between tasks but also preventing
  to run before previous task finishes delivering output (which could be input for current task).

  ```yaml
    - name: maven-build
      taskRef:
        name: maven
      runAfter:
        - fetch-code # task maven-build shall wait to fetch-code to finish
  
    - name: container-image
      taskRef:
        name: buildah
      runAfter:
        - maven-build # container-image will wait for maven-build to finish
  
    - name: deploy-app
      taskRef:
        name: kubernetes-actions
      runAfter:
        - container-image # image must be deployed to registry before it is used to deploy service 
  ```
- This was not a requirement, but since pipelines are meant to run several times a day (for every push to a repo)
  it might be convenient to keep all dependencies downloaded avoiding doing it each time. It might be necessary to
  clean it up periodically since some dependencies will become obsolete after some time.

  ```yaml
    - name: maven-local-repo
      workspace: workspace # this is defined at workspaces section of pipeline and binded at runner definition
  ```

As mentioned above, there are different ways to run a pipeline: using a [***`PipelineRun`***](
https://tekton.dev/docs/pipelines/pipelineruns/) (similar to task runners) or through Event Listeners and Triggers.

PipelineRun references pipeline to run and parameters that are used by task in it or necessary workspaces' sources.

```yaml
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  generateName: pipelinerun-cicd-
  namespace: <namespace-name>
spec:
  serviceAccountName: tekton-sa
  pipelineRef:
    name: pipeline-cicd # this is the name of pipeline to run, must exist in namespace
  params: # top level parameters definition, will be passed to pipeline and tasks will use from it
    - name: repo-url
      value: https://github.com/sacnavi/adoptpet.git
    - name: maven-image
      value: gcr.io/cloud-builders/mvn@sha256:8f38a2667125a8d83f6e1997847fedb6a06f041c90e2244884153d85d95f869b
    - name: container-image
      value: docker.io/sacnavi/adoptpetsvc:v9
  workspaces: # workspaces used, source for each one is specified
    - name: maven-settings
      configmap: # workspaces can use a configmap, a persistent volume claim or a secret
        name: maven-settings
    - name: workspace
      persistentVolumeClaim:
        claimName: workspace-ricp
    - name: dockerconfig
      secret:
        secretName: dockerconfig-secret
```

When a pipeline runs, a pod for every task is deployed and at the end, a summary can be shown so status for each
task can be reviewed.

![Pipeline result](../_resources/pipeline-result.png "Pipeline summary")

## Running pipeline on every push to repository
As mentioned above, pipeline has to be run every time new code is added to repository so it goes through the whole
process to deployment.

This means we need to link source control repository and cluster running pipeline and tasks.

### Trigger
A [***`TriggerTemplate`***](https://tekton.dev/docs/triggers/triggertemplates/) specifies a blueprint for the object
to instantiate/execute.

```yaml
apiVersion: triggers.tekton.dev/v1alpha1
kind: TriggerTemplate
metadata:
  name: tekton-trigger-template-cicd # name for the resource
  namespace: <namespace-name>
spec:
  params: # list of parameters made available by the template, can be passed to underlying resource
    - name: <parameter-name>
      description: <parameter-description>
  resourcetemplates: # resource to fire
    - apiVersion: tekton.dev/v1beta1
      kind: PipelineRun # type of resource to fire, could also be a taskrun
      metadata:
        generateName: pipelinerun-cicd-
      spec:
        serviceAccountName: tekton-sa
        pipelineRef:
          name: pipeline-cicd # name of the associated pipeline
        params:
          - name: <parameter-name-in-pipeline> # see repo-url in PipelineRun definition at previous section
            value: $(tt.params.<parameter-name>) # this name is one of listed above, in TriggerTemplate->spec->params
        workspaces: # list of workspaces to be used in pipeline and source for everyone (pvc, secret, configmap, etc.)
          - name: <workspace-name> # this is how workspace is referenced
            <source-type>: # persistent volume claim, secret, config map or empty directory
              name: <source-name> # reference of source within namespace, should exist and contain necessary information
```
Up to this point, pipeline can be run through described template, now it is necessary to provide values for all 
defined parameters in it.

A [***`TriggerBinding`***](https://tekton.dev/docs/triggers/triggerbindings/) allows fields extraction from an event 
payload and bind them to named parameters that can then be used in a TriggerTemplate. 

Trigger uses the parameter name to match TriggerBinding params to TriggerTemplate params. In order to pass information,
the param name used in the binding must match the param name used in the template.
```yaml
apiVersion: triggers.tekton.dev/v1alpha1
kind: TriggerBinding
metadata:
  name: tekton-trigger-binding-cicd
  namespace: <namespace-name>
spec:
  params: # list of parameters to bind, values can be literal or a variable coming from some event
    - name: repo-url # as mentioned, this name must match exactly the name defined in template to be properly bound
      value: $(body.repository.clone_url) # this name must match some value from event listener payload, see below
```
A source of this information is now needed so pipeline can be run with proper values.

For this project a GitHub Webhook is used, so now it is necessary to add a mechanism that catches these requests and
starts processing incoming data and passing them to pipeline through templates.

### Event listener
A [***`ClusterInterceptor`***](https://tekton.dev/docs/triggers/clusterinterceptors/) specifies an external Kubernetes 
service running custom business logic that receives the event payload from the EventListener via an HTTP request and
returns a processed version of the payload along with an HTTP 200 response.

The ClusterInterceptor can also halt processing if the event payload does not meet criteria you have configured as well
as add extra fields that are accessible in the EventListener's top-level extensions field to other Interceptors and
ClusterInterceptors chained with it and the associated TriggerBinding.

Interceptors can be defined either as a single resource, so can be reused across different event listeners, or inline
within event listener definition, this second approach is used for this example.

An [***`EventListener`***](https://tekton.dev/docs/triggers/eventlisteners/) is a Kubernetes object that listens for 
events at a specified port on a Kubernetes cluster.

It exposes an addressable sink that receives incoming event and specifies one or more Triggers. The sink is a Kubernetes
service running the sink logic inside a dedicated Pod.

```yaml
apiVersion: triggers.tekton.dev/v1beta1
kind: EventListener
metadata:
  name: event-listener-cicd
  namespace: <namespace-name>
spec:
  serviceAccountName: tekton-triggers-sa # service account granted with privileges to handle several
  # resources in triggers.tekton.dev apiGroup, configurations is beyond this document's scope
  triggers:
    - name: github-listener
      interceptors: # interceptors defined for this event listener
        - ref:
            name: "github"
            kind: ClusterInterceptor
            apiVersion: triggers.tekton.dev
          params:
            - name: "eventTypes"
              value: ["push"] # will only process pushes to repository, ignoring any other type 
      bindings:
        - ref: tekton-trigger-binding-cicd # this is the name of binding defined above
      template:
        ref: tekton-trigger-template-cicd # this is the template to bind to events 
```
As this pod and service are running within the cluster, it is still necessary to expose an endpoint on internet so
GitHub webhook can reach to it and send event information.

An [***`Ingress`***](https://kubernetes.io/docs/concepts/services-networking/ingress/) exposes HTTP and HTTPS routes
from outside the cluster to services within the cluster. Traffic routing is controlled by rules defined on the Ingress
resource.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-el
  namespace: <namespace-name>
  annotations:
    # Specifies the entry points for the Traefik router, tells Traefik to use the 'web' entry point for this Ingress.
    traefik.ingress.kubernetes.io/router.entrypoints: web
  rules:
    - host: el-event-listener-cicd.example.com # this is the url exposed by the cluster
      http:
        paths:
          - path: /path-on-url # maps to specific resource so several services can be accessed through same host 
            pathType: Prefix
            backend:
              service:
                name: el-event-listener-cicd
                port:
                  number: 8080
```
Once all these resources are configured and running, a webhook shall be created on GitHub repository to send information
to `el-event-listener-cicd.example.com/path-on-url`, setup is beyond scope of this documentation, please see
[Webhooks Guide](https://docs.github.com/en/webhooks) for detailed information.

Following image shows how all these resources are logically connected, including names defined in previous snippets.

![Workflow from GitHub to Event listener to trigger to Pipeline](../_resources/git-to-pipeline.png "Event workflow")

## Want to learn more?
### Reference Documentation
For further reference, please consider the following sections, inline links were also provided as part of contents in
this document.

* [Tekton project](https://tekton.dev/)
* [Tekton tasks repository](https://hub.tekton.dev/)
* [YAML specification](https://yaml.org/)
