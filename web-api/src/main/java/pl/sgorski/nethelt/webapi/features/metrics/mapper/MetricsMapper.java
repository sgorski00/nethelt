package pl.sgorski.nethelt.webapi.features.metrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.metrics.domain.Metrics;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.MetricsResponse;

@Mapper(componentModel = "spring")
public interface MetricsMapper {
  @Mapping(target = "bucketSeconds", expression = "java(metrics.bucket().toSeconds())")
  MetricsResponse toResponse(Metrics metrics);
}
