package com.genius.primavera.domain.model.post;

import com.genius.primavera.domain.converter.PostStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
import com.genius.primavera.domain.model.user.User;

import lombok.*;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POST")
public class Post extends BaseEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "WRITER_ID", updatable = false)
    private User writer;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENTS")
    private String contents;

    @Column(name = "STATUS")
    @Convert(converter = PostStatusAttributeConverter.class)
    private PostStatus status;

    public String getWriterName() {
        return writer.getNickname();
    }
}