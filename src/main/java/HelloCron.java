import com.uber.cadence.TerminateWorkflowExecutionRequest;
import com.uber.cadence.WorkflowExecution;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowClientOptions;
import com.uber.cadence.serviceclient.ClientOptions;
import com.uber.cadence.serviceclient.WorkflowServiceTChannel;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.WorkerFactory;

/**
 * Requires a local instance of Cadence server to be running.
 */
public class HelloCron {
    public static void main(String[] args) throws InterruptedException {
        var cadenceService = new WorkflowServiceTChannel(ClientOptions.defaultInstance());
        var workflowClient = WorkflowClient.newInstance(
            cadenceService,
            WorkflowClientOptions.newBuilder().setDomain(Constants.DOMAIN_NAME).build());

        CreateWorkflow(workflowClient);
        RunWorkflow(workflowClient);

        Sleep();

        TerminateWorkflow(cadenceService);
        System.exit(0);
    }

    private static void CreateWorkflow(WorkflowClient workflowClient) {
        // Get worker to poll the task list.
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(Constants.TASK_LIST);

        // Workflows are stateful. So you need a type to create instances.
        worker.registerWorkflowImplementationTypes(CronWorkflowImpl.class);

        // Activities are stateless and thread safe. So a shared instance is used.
        worker.registerActivitiesImplementations(new GreetingActivityImpl());


        // Start listening to the workflow and activity task lists.
        factory.start();
    }

    private static void RunWorkflow(WorkflowClient workflowClient) {
        // Start a workflow execution async. Usually this is done from another program.
        CronWorkflow workflow = workflowClient.newWorkflowStub(CronWorkflow.class);
        WorkflowClient.start(workflow::greetPeriodically, "World");
        System.out.println("Cron workflow is running");
    }

    private static void Sleep() throws InterruptedException {
        // Cron workflow will not stop until it is terminated or cancelled.
        // So we wait some time to see cron run twice then terminate the cron workflow.
        Thread.sleep(90000);
    }

    private static void TerminateWorkflow(WorkflowServiceTChannel cadenceService) {
        // execution without RunID set will be used to terminate current run
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(Constants.CRON_WORKFLOW_ID);
        TerminateWorkflowExecutionRequest request = new TerminateWorkflowExecutionRequest();
        request.setDomain(Constants.DOMAIN_NAME);
        request.setWorkflowExecution(execution);

        try {
            cadenceService.TerminateWorkflowExecution(request);
            System.out.println("Cron workflow is terminated");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}