package com.kos.dto;

import java.util.List;

//RestaurantSetup POJO
public class RestaurantSetup {

 private String restaurantName;
 private String address;
 private String phone;
 private String email;
 private List<StaffSetup> staff;

 public RestaurantSetup() {
 }

 public RestaurantSetup(String restaurantName, String address, String phone, String email, List<StaffSetup> staff) {
     this.restaurantName = restaurantName;
     this.address = address;
     this.phone = phone;
     this.email = email;
     this.staff = staff;
 }

 public String getRestaurantName() {
     return restaurantName;
 }

 public void setRestaurantName(String restaurantName) {
     this.restaurantName = restaurantName;
 }

 public String getAddress() {
     return address;
 }

 public void setAddress(String address) {
     this.address = address;
 }

 public String getPhone() {
     return phone;
 }

 public void setPhone(String phone) {
     this.phone = phone;
 }

 public String getEmail() {
     return email;
 }

 public void setEmail(String email) {
     this.email = email;
 }

 public List<StaffSetup> getStaff() {
     return staff;
 }

 public void setStaff(List<StaffSetup> staff) {
     this.staff = staff;
 }
}
