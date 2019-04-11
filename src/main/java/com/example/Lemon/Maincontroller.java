package com.example.Lemon;


import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class Maincontroller {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SeriesRepository seriesRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private ComicRepository comicRepository;


    @GetMapping("/test")
    public String test(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = new User();
        user.setId(1);
        user.setName("weicai");
        user.setPassword("Caiwei12");
        user.setIssuperuser(false);
        user.setProfileimage("xxx");
        userRepository.save(user);
        return "signin";
    }

    @GetMapping("/")
    public String aaa(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") != null) {
            return "redirect:/hello";
        }
        return "signin";
    }


    @GetMapping("/login")
    public String asd(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") != null) {
            return "redirect:/hello";
        }
        return "signin";
    }

    @PostMapping("/login")
    public String loginSubmit(HttpServletRequest request) {
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        Iterable<User> a = userRepository.findAll();
        HttpSession session = request.getSession();
        System.out.println(a);
        for (User u : a) {
            if (u.getName().equals(name) && u.getPassword().equals(password)) {
                if(u.getIssuperuser()){
                    return "superuser";
                }
                session.setAttribute("Name",name);
                session.setAttribute("login_state",1);
                return "redirect:/home";
            }
        }
        session.setAttribute("login_state", 2);
        return "signin";
    }



    @GetMapping("/signUp")
    public String signup(HttpServletRequest request) {
        return "signUp";
    }

    @PostMapping("/signUp")
    public String signup2(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        if(password.length()<6 ||password.length()>16){
            session.setAttribute("status",3);
        }

        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);
        user.setProfileimage(null);
        user.setIssuperuser(false);

        Iterable<User> a = userRepository.findAll();

        for (User u : a) {
            if (u.getName().equals(user.getName())) {//username exists
                session.setAttribute("state", 2);
                return "signup";
            }
            if(u.getEmail().equals((user.getEmail()))){
                session.setAttribute("state", 4);
                return "signup";
            }
        }

        userRepository.save(user);
        session.setAttribute("username", user.getName());
        session.setAttribute("username",1);
        return "redirect:/hello";
    }


    @RequestMapping("/home")
    public String home(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") == null) {
            return "redirect:/login";
        }
        return "home";
    }

    @RequestMapping("/series")
    public String seriesinfo(HttpServletRequest request) {
        Iterable<Series> a = seriesRepository.findAll();
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") == null) {
            return "redirect:/login";
        }
        int seriesid = Integer.parseInt(request.getParameter("id"));

        for (Series s : a) {
            if (s.getId().equals(seriesid)) {
                session.setAttribute("state", 1);
                session.setAttribute("series",s);
                return "series";
            }
        }
        session.setAttribute("state", 2);
        session.setAttribute("series",null);
        return "series";
    }


    @RequestMapping("/read")
    public String pageinfo(HttpServletRequest request) {

        HttpSession session = request.getSession();
        if (session.getAttribute("Name") == null) {
            return "redirect:/login";
        }

        Iterable<Page> a = pageRepository.findAll();
        Iterable<Comic> b = comicRepository.findAll();
        int seriesid=0;
        int comicindex=0;

        int comicid = Integer.parseInt(request.getParameter("id"));
        int pageindex = Integer.parseInt(request.getParameter("index"));

        for(Comic c:b){
            if(c.getId().equals(comicid)) {
                seriesid = c.getSeriesid();
                comicindex=c.getIndexs();
                break;
            }
        }

        List<Page> list2 = pageRepository.findByComicid(comicid);
        List<Comic> list3 =comicRepository.findBySeriesid(seriesid);


        session.setAttribute("chapternumber",list3.size());
        session.setAttribute("comicindex",comicindex);
        session.setAttribute("pagenumber",list2.size());

        List<Page> list = pageRepository.findByComicidAndIndexs(comicid,pageindex);

        if(list.size() == 0){
            session.setAttribute("page",null);
            session.setAttribute("status",2);
        }
        else{
            session.setAttribute("page",list.get(0));
            session.setAttribute("status",1);
        }

        return "read";

    }


}
