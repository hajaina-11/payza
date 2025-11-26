package io.granix.user.database.entity;

import io.granix.common.converter.MapToJsonConverter;
import io.granix.user.database.entity.utils.UserIDPieceType;
import io.granix.user.database.entity.utils.UserStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "pz_user")
public class UserEntity extends PanacheEntityBase {
    // System information
    @Id
    public UUID id;

    public LocalDateTime createdAt;

    public boolean isRGPD;

    @Enumerated(EnumType.STRING)
    public UserStatus status;

    @Column(nullable = false)
    public String passwordHashed;

    //Personal information
    public String firstname;

    public String lastname;

    public String gender;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    public Set<Role> roles = new HashSet<>();

    @Column(nullable = false, unique = true)
    public String msisdnEncrypted;

    public String emailEncrypted;

    public String birthday;

    @Enumerated(EnumType.STRING)
    public UserIDPieceType identityPieceType;

    public String identityNumber;

    @Convert(converter = MapToJsonConverter.class)
    public Map<String, Object> metadata;
}
