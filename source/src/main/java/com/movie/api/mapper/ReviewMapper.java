package com.movie.api.mapper;

import com.movie.api.dto.review.ReviewDto;
import com.movie.api.dto.review.ReviewNotificationDto;
import com.movie.api.form.review.CreateReviewForm;
import com.movie.api.form.review.UpdateReviewForm;
import com.movie.api.storage.model.Review;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AccountMapper.class})
public interface ReviewMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "author", target = "author", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "movieId", target = "movieId")
    @Mapping(source = "rate", target = "rate")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "totalLike", target = "totalLike")
    @Mapping(source = "totalDislike", target = "totalDislike")
    @Mapping(source = "toxicSpans", target = "toxicSpans")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToReviewDto")
    ReviewDto entityToReviewDto(Review review);

    @IterableMapping(elementTargetType = ReviewDto.class, qualifiedByName = "entityToReviewDto")
    List<ReviewDto> fromEntityToReviewDtoList(List<Review> reviews);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "movieId", target = "movieId")
    @Mapping(source = "rate", target = "rate")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "author", target = "author", qualifiedByName = "entityToAccountNotificationDto")
    @Mapping(source = "toxicSpans", target = "toxicSpans")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToReviewNotificationDto")
    ReviewNotificationDto entityToReviewNotificationDto(Review review);

    @Mapping(source = "rate", target = "rate")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    Review fromCreateReviewFormToEntity(CreateReviewForm form);

    @Mapping(source = "rate", target = "rate")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateReviewFormToEntity(UpdateReviewForm form, @MappingTarget Review review);
}
