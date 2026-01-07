package com.kos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.Item;
import com.kos.service.InventoryService;


@RestController
public class InventoryController {
	
	@Autowired
	InventoryService itemService;
	
	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<String>("Ok",HttpStatus.OK);
	}
	
	@PostMapping("/addItem")
	public ResponseEntity<Item> addItem(@RequestBody Item item){
		
		return new ResponseEntity<Item>(itemService.addItem(item), HttpStatus.OK);
		
	}

}
