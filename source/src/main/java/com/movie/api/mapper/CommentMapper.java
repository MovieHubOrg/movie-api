package com.movie.api.mapper;

import com.movie.api.dto.comment.CommentDto;
import com.movie.api.form.comment.CreateCommentForm;
import com.movie.api.form.comment.UpdateCommentForm;
import com.movie.api.storage.model.Comment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieItemMapper.class, AccountMapper.class})
public interface CommentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "movieItem", target = "movieItem", qualifiedByName = "entityToMovieItemAutoCompleteDto")
    @Mapping(source = "movieId", target = "movieId")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "entityToParentDto")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "totalLike", target = "totalLike")
    @Mapping(source = "totalDislike", target = "totalDislike")
    @Mapping(source = "totalChildren", target = "totalChildren")
    @Mapping(source = "isPinned", target = "isPinned")
    @Mapping(source = "authorInfo", target = "authorInfo")
    @Mapping(source = "replyToInfo", target = "replyToInfo")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToCommentDto")
    CommentDto entityToCommentDto(Comment comment);

    @IterableMapping(elementTargetType = CommentDto.class, qualifiedByName = "entityToCommentDto")
    List<CommentDto> fromEntityToCommentDtoList(List<Comment> comments);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "authorInfo", target = "authorInfo")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToParentDto")
    CommentDto entityToParentDto(Comment comment);

    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    Comment fromCreateCommentFormToEntity(CreateCommentForm form);

    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateCommentFormToEntity(UpdateCommentForm form, @MappingTarget Comment comment);
}
