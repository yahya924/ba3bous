//package com.igatn.extranet.domain;
//
//import lombok.*;
//
//import javax.persistence.*;
//import java.util.Set;
//
//@Data
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//public class UserRoleRegistry {
//
//    private static final long serialVersionUID = 3L;
//
//    public UserRoleRegistry(User user, Role role) {
//        this.user = user;
//        this.role = role;
//        if (this.user != null)
//            this.userName = this.user.getUsername();
//        if (this.role != null)
//            this.roleType = this.role.getType();
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "role_id")
//    private Role role;
//
//    private String userName;
//
//    private String roleType;
//
//
//}