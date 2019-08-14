package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.UserStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
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
public class User {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "STATUS")
    @Convert(converter = UserStatusAttributeConverter.class)
    private UserStatus status;

    // 단방향
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CONNECTION_ID", referencedColumnName = "ID")
    private UserConnection connection;

    @ManyToMany(targetEntity = Role.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLE", joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")}, inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")})
    private Set<Role> roles = new HashSet<>();
}