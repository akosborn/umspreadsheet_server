package com.umspreadsheet.controller;

import com.umspreadsheet.domain.User;
import com.umspreadsheet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController
{
    private UserService userService;

    @Autowired
    public AuthController(UserService userService)
    {
        this.userService = userService;
    }

    @RequestMapping("/register")
    public String createUser(Model model)
    {
        model.addAttribute("user", new User());
        return "/auth/register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String saveUser(User user, RedirectAttributes redirectAttributes)
    {
        User savedUser = userService.save(user);
        if (savedUser == null)
        {
            redirectAttributes.addFlashAttribute("signup", "false");
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("signup", "You have successfully signed up.");
        return "redirect:/login";
    }
}