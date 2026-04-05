package com.movie.api.service;

import com.movie.api.dto.video.VideoLibraryDto;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.mapper.VideoLibraryMapper;
import com.movie.api.storage.model.ServerConfig;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.repository.ServerConfigRepository;
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

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    public VideoLibraryDto updateVideoLibrary(UpdateVideoForm form) {
        log.warn("Start updating video ID: {}", form.getId());
        log.warn(form.getContent());
        log.warn("Server number: {}", form.getServerNumber());

        ServerConfig serverConfig = serverConfigRepository.findByServerNumber(form.getServerNumber()).orElse(null);
        if (serverConfig == null) {
            log.warn("Server config not found for server number: {}", form.getServerNumber());
            return null;
        }

        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getId()).orElse(null);
        if (videoLibrary != null) {
            videoLibraryMapper.fromUpdateVideoFormToEntity(form, videoLibrary);
            videoLibrary.setServerConfig(serverConfig);
            videoLibrary = videoLibraryRepository.save(videoLibrary);
            return videoLibraryMapper.entityToVideoLibraryShortDto(videoLibrary);
        }
        log.warn("End updating video ID: {}", form.getId());
        return null;
    }
}
