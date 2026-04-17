package ee.aleksale.releaseapp.model.mapper;

import ee.aleksale.releaseapp.model.domain.ReleaseEntity;
import ee.aleksale.releaseapp.model.dto.Release;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReleaseMapper {

  ReleaseMapper INSTANCE = Mappers.getMapper(ReleaseMapper.class);

  Release toRelease(ReleaseEntity entity);

}
