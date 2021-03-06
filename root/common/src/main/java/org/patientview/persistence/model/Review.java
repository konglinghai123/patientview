package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.patientview.persistence.model.enums.ReviewSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.text.ParseException;
import java.util.Date;

/**
 * Models a research study criteria that is available to a user
 */
@Entity
@Data
@Table(name = "pv_reviews")
@NoArgsConstructor
public class Review extends BaseModel {

    /**
     * Copy an existing review (from facebook API)
     * and ingest into PV db
     *
     * @param review the review to copy
     * @throws ParseException Thrown when cannot parse the date
     */
    public Review(Review review) throws ParseException {
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ");
            DateTime dt = formatter.parseDateTime(review.getCreated_time());

            this.setCreatedDate(dt.toDate());
        } catch (Exception e) {

        }
        this.setReviewText(org.apache.commons.lang3.StringUtils.stripAccents(review.getReview_text())
                .replaceAll("[^\\p{ASCII}]", " "));
        this.setReviewerName(org.apache.commons.lang3.StringUtils.stripAccents(review.getReviewer().getName())
                .replaceAll("[^\\p{ASCII}]", " "));
        this.setRating(review.getRating());
        this.setReviewSource(review.getReviewSource());
        this.setExcluded(false);
    }

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "rating")
    private int rating;

    @Column(name = "excluded")
    private Boolean excluded;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "review_source")
    @Enumerated(EnumType.STRING)
    private ReviewSource reviewSource;


    /**
     * Transient data to get from facebook
     **/
    @Transient
    @JsonIgnore
    private String review_text;

    @Transient
    @JsonIgnore
    private Reviewer reviewer;

    @Transient
    @JsonIgnore
    private String created_time;

    @JsonIgnore
    public void createAndSetReviewer(String name){
        this.reviewer = new Reviewer();
        this.reviewer.setName(name);
    }

    @Data
    class Reviewer {
        private String name;
        private String id;
    }
}
