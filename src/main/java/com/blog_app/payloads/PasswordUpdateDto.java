package com.blog_app.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter

public class PasswordUpdateDto {
	
	private String currentPassword;
	
	@NotEmpty
	@Size(min=5 , message = "Password should be minimum 5 char.")
    private String newPassword;
	
	@NotEmpty
	@Size(min=5,message = "new password doesnt match")
	private String confirmPassword;


}
