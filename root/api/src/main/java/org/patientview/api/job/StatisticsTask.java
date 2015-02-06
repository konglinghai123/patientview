package org.patientview.api.job;

import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.Timer;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the class that executes the job to collate the stats.
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Component
public class StatisticsTask {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsTask.class);

    @Inject
    private Timer timer;

    @Inject
    private GroupStatisticService groupStatisticService;

    /**
     * TODO this needs hardening and possibly JMX bean to run the job
     * Statistics for the previous month, collected on the first day of the month.
     * "0 0 12 1 * ?" = 12:00 on the first day of the month
     */
    @Scheduled(cron = "0 0 12 1 * ?")
    public void generatePreviousMonthStatistics() {
        Calendar calendar = timer.getCurrentDate();

        // Only run of the first day of the month
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {

            // set end date to now
            Date endDate = calendar.getTime();

            // set start date to one month ago
            calendar.roll(Calendar.MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startDate = calendar.getTime();

            LOG.info("Creating statistics (monthly) for period " + startDate.toString() + " to " + endDate.toString());
            groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);
        }
    }

    /**
     * Statistics for the current month, run every day at 1am
     */
    //@Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    @Scheduled(cron = "0 0 1 * * ?") // every day at 01:00
    public void generateThisMonthStatistics() {
        Calendar calendar = timer.getCurrentDate();

        // set end date to now
        Date endDate = calendar.getTime();

        // Set the start date to the beginning of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        LOG.info("Creating statistics (every 1am) for period " + startDate.toString() + " to " + endDate.toString());
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);
    }
}
