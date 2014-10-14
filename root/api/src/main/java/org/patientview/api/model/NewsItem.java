package org.patientview.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/10/2014
 */
public class NewsItem {

    private Long id;
    private String heading;
    private String story;
    private List<NewsLink> newsLinks;
    private Date created;
    private Date lastUpdate;
    private BaseUser lastUpdater;
    private boolean edit;
    private boolean delete;

    public NewsItem() {

    }

    public NewsItem(org.patientview.persistence.model.NewsItem newsItem) {
        setId(newsItem.getId());
        setHeading(newsItem.getHeading());
        setStory(newsItem.getStory());

        setNewsLinks(new ArrayList<NewsLink>());
        if (newsItem.getNewsLinks() != null) {
            for (org.patientview.persistence.model.NewsLink newsLink : newsItem.getNewsLinks()) {
                getNewsLinks().add(new NewsLink(newsLink));
            }
        }

        setCreated(newsItem.getCreated());
        setLastUpdate(newsItem.getLastUpdate());
        if (newsItem.getLastUpdater() != null) {
            setLastUpdater(new BaseUser(newsItem.getLastUpdater()));
        }
        setEdit(newsItem.isEdit());
        setDelete(newsItem.isDelete());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public List<NewsLink> getNewsLinks() {
        return newsLinks;
    }

    public void setNewsLinks(List<NewsLink> newsLinks) {
        this.newsLinks = newsLinks;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public BaseUser getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(BaseUser lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
