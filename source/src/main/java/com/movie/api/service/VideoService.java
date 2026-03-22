package com.movie.api.service;

import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.mapper.VideoLibraryMapper;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.repository.VideoLibraryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VideoService {
    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private VideoLibraryMapper videoLibraryMapper;

    public void updateVideoLibrary(UpdateVideoForm form) {
        log.warn("Start updating video ID: {}", form.getId());
        log.warn(form.getContent());
        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getId()).orElse(null);
        if (videoLibrary != null) {
            videoLibraryMapper.fromUpdateVideoFormToEntity(form, videoLibrary);
            videoLibraryRepository.save(videoLibrary);
        }
        log.warn("End updating video ID: {}", form.getId());
    }
}
