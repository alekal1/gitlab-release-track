package ee.aleksale.releaseapp.model.mapper;

import ee.aleksale.releaseapp.model.domain.GitlabProjectEntity;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GitlabProjectMapper {
  GitlabProjectMapper INSTANCE = Mappers.getMapper(GitlabProjectMapper.class);

  GitlabProject toGitlabProject(GitlabProjectEntity entity);
  GitlabProjectEntity toGitlabProjectEntity(GitlabProject dto);

}
