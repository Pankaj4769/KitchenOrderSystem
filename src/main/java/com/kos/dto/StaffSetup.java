package com.kos.dto;

//StaffSetup POJO
public class StaffSetup {

 private String name;
 private String mobile;
 private String email;
 private UserRole role;

 public StaffSetup() {
 }

 public StaffSetup(String name, String mobile, String email, UserRole role) {
     this.name = name;
     this.mobile = mobile;
     this.email = email;
     this.role = role;
 }

 public String getName() {
     return name;
 }

 public void setName(String name) {
     this.name = name;
 }

 public String getMobile() {
     return mobile;
 }

 public void setMobile(String mobile) {
     this.mobile = mobile;
 }

 public String getEmail() {
     return email;
 }

 public void setEmail(String email) {
     this.email = email;
 }

 public UserRole getRole() {
     return role;
 }

 public void setRole(UserRole role) {
     this.role = role;
 }
}
