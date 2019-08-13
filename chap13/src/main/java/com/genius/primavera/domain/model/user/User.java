package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.UserStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USER")
public class User extends BaseEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", updatable = false)
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "STATUS")
    @Convert(converter = UserStatusAttributeConverter.class)
    private UserStatus status;

    //@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //@JoinColumn(name = "EMAIL")
    @Transient
    private UserConnection connection;

    //@ManyToMany(fetch = FetchType.LAZY)
    //@JoinTable(name = "USER_ROLE", joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")}, inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")})
    @Transient
    private List<Role> roles;
}