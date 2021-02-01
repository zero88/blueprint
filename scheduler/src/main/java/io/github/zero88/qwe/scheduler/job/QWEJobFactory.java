package io.github.zero88.qwe.scheduler.job;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import io.github.zero88.qwe.component.HasSharedData;
import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.scheduler.SchedulerConfig;
import io.vertx.core.shareddata.SharedData;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents for QWE Job factory to create new QWE job instance
 *
 * @see JobFactory
 * @see SharedData
 * @see QWEJob
 */
@RequiredArgsConstructor
public final class QWEJobFactory extends SimpleJobFactory implements JobFactory, HasSharedData {

    @Getter
    @Accessors(fluent = true)
    private final SharedDataLocalProxy sharedData;
    private final SchedulerConfig config;

    @Override
    @SuppressWarnings("rawtypes")
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Job job = super.newJob(bundle, scheduler);
        final Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        if (QWEJob.class.isAssignableFrom(jobClass)) {
            return ((QWEJob) job).init(sharedData, config);
        }
        return job;
    }

}