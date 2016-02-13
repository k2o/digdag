package io.digdag.core.schedule;

import java.util.List;
import java.util.TimeZone;
import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import com.google.inject.Inject;
import com.google.common.base.Optional;
import io.digdag.spi.ScheduleTime;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.client.config.ConfigFactory;
import io.digdag.core.repository.RepositoryStoreManager;
import io.digdag.core.repository.StoredWorkflowDefinitionWithRepository;
import io.digdag.core.repository.ResourceNotFoundException;
import io.digdag.core.repository.WorkflowDefinition;
import io.digdag.core.session.Session;
import io.digdag.core.session.StoredSessionAttemptWithSession;
import io.digdag.core.workflow.SessionAttemptConflictException;
import io.digdag.core.workflow.AttemptRequest;
import io.digdag.core.workflow.AttemptBuilder;
import io.digdag.core.workflow.WorkflowExecutor;

public class ScheduleHandler
{
    private final ConfigFactory cf;
    private final RepositoryStoreManager rm;
    private final AttemptBuilder attemptBuilder;
    private final WorkflowExecutor exec;

    @Inject
    public ScheduleHandler(
            ConfigFactory cf,
            RepositoryStoreManager rm,
            AttemptBuilder attemptBuilder,
            WorkflowExecutor exec)
    {
        this.cf = cf;
        this.rm = rm;
        this.attemptBuilder = attemptBuilder;
        this.exec = exec;
    }

    public StoredSessionAttemptWithSession start(StoredWorkflowDefinitionWithRepository def,
            ZoneId timeZone, ScheduleTime time, Optional<String> retryAttemptName)
            throws ResourceNotFoundException, SessionAttemptConflictException
    {
        AttemptRequest ar = attemptBuilder.buildFromStoredWorkflow(
                retryAttemptName,
                def,
                cf.create(),  // TODO add schedules.params and copy it here
                time);

        return exec.submitWorkflow(def.getRepository().getSiteId(),
                ar, def);
    }
}
