package com.backbase.accelerators.mapper;

import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.model.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface  ScheduleMapper {
    Schedule toSchedule(ScheduledBatchOrderItem scheduledBatchOrderItem);
}
