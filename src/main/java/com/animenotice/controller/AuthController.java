package com.animenotice.controller;

import com.animenotice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout,
                        @RequestParam(required = false) String error,
                        Model model) {
        if (logout != null) model.addAttribute("logoutMessage", "ログアウトしました");
        if (error != null) model.addAttribute("errorMessage", "メールアドレスまたはパスワードが正しくありません");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "パスワードが一致しません");
            return "redirect:/register";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "パスワードは8文字以上で入力してください");
            return "redirect:/register";
        }
        try {
            userService.register(email, password);
            redirectAttributes.addFlashAttribute("successMessage", "登録しました。ログインしてください");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
}
