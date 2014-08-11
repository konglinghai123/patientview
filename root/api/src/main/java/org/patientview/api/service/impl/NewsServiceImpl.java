package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class NewsServiceImpl extends AbstractServiceImpl<NewsServiceImpl> implements NewsService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private NewsItemRepository newsItemRepository;

    @Inject
    private EntityManager entityManager;

    public NewsItem add(final NewsItem newsItem) {

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        creator = userRepository.findOne(creator.getId());

        if (!CollectionUtils.isEmpty(newsItem.getNewsLinks())) {
            for (NewsLink newsLink : newsItem.getNewsLinks()) {
                if (newsLink.getGroup() != null) {
                    newsLink.setGroup(groupRepository.findOne(newsLink.getGroup().getId()));
                }

                if (newsLink.getRole() != null) {
                    newsLink.setRole(roleRepository.findOne(newsLink.getRole().getId()));
                }
                newsLink.setNewsItem(newsItem);
                newsLink.setCreator(creator);
            }
        }
        NewsItem persistedNewsItem = newsItemRepository.save(newsItem);

        return persistedNewsItem;
    }

    public NewsItem get(final Long newsItemId) {
        return newsItemRepository.findOne(newsItemId);
    }

    public NewsItem save(final NewsItem newsItem) throws ResourceNotFoundException {

        NewsItem entityNewsItem = newsItemRepository.findOne(newsItem.getId());
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException("Could not find news {}" + newsItem.getId());
        }

        entityNewsItem.setHeading(newsItem.getHeading());
        entityNewsItem.setStory(newsItem.getStory());
        return newsItemRepository.save(entityNewsItem);
    }

    public void delete(final Long newsItemId) {
        newsItemRepository.delete(newsItemId);
    }

    public Page<NewsItem> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }

        // get both role and group news
        PageRequest pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> roleNews = newsItemRepository.findRoleNewsByUser(entityUser, pageableAll);
        Page<NewsItem> groupNews = newsItemRepository.findGroupNewsByUser(entityUser, pageableAll);

        // combine results
        Set<NewsItem> newsItemSet = new HashSet<>();
        if (roleNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(roleNews.getContent());
        }
        if (groupNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(groupNews.getContent());
        }
        List<NewsItem> newsItems = new ArrayList<>(newsItemSet);

        // sort combined list
        Collections.sort(newsItems);

        // manually do pagination
        int left = pageable.getOffset();
        int right;

        if ((left + pageable.getPageSize()) > newsItems.size()) {
            right = newsItems.size();
        } else {
            right = pageable.getPageSize();
        }

        List<NewsItem> pagedNewsItems = new ArrayList<>();

        if (!newsItems.isEmpty()) {
            pagedNewsItems = newsItems.subList(left, right);
        }

        return new PageImpl<>(pagedNewsItems, pageable, newsItems.size());
    }

    public void addGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException("Could not find news {}" + newsItemId);
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Could not find news {}" + groupId);
        }

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Group newsLinkGroup = newsLink.getGroup();
            if (newsLink.getGroup() != null && (newsLinkGroup.getId() == entityGroup.getId())) {
                found = true;
            }
        }

        if (!found) {
            User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            creator = userRepository.findOne(creator.getId());

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setGroup(entityGroup);
            newsLink.setCreator(creator);

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    public void removeGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException("Could not find news {}" + newsItemId);
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Could not find news {}" + groupId);
        }

        NewsLink tempNewsLink = null;
        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            if (newsLink.getGroup() != null && (newsLink.getGroup().getId().equals(entityGroup.getId()))) {
                tempNewsLink = newsLink;
            }
        }

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }

    public void addRole(Long newsItemId, Long roleId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException("Could not find news {}" + newsItemId);
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException("Could not find news {}" + roleId);
        }

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Role newsLinkRole = newsLink.getRole();
            if (newsLink.getRole() != null && (newsLinkRole.getId() == entityRole.getId())) {
                found = true;
            }
        }

        if (!found) {
            User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            creator = userRepository.findOne(creator.getId());

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setRole(entityRole);
            newsLink.setCreator(creator);

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    public void removeRole(Long newsItemId, Long roleId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException("Could not find news {}" + newsItemId);
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException("Could not find news {}" + roleId);
        }

        NewsLink tempNewsLink = null;
        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            if (newsLink.getRole() != null && (newsLink.getRole().getId().equals(entityRole.getId()))) {
                tempNewsLink = newsLink;
            }
        }

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }
}
