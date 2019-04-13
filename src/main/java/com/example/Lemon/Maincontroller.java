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



    @GetMapping("/")
    public String aaa(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") != null) {
            return "redirect:/home";
        }
        return "signIn";
    }


    @GetMapping("/login")
    public String asd(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") != null) {
            return "redirect:/home";
        }
        return "signIn";
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
        return "signIn";
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
            session.setAttribute("signup_state",3);
            return "signup";
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
                session.setAttribute("signup_state", 2);
                return "signup";
            }
            System.out.println(u.getEmail());
            if(u.getEmail().equals(user.getEmail())){
                session.setAttribute("signup_state", 4);
                return "signup";
            }
        }

        userRepository.save(user);
        session.setAttribute("Name", user.getName());
        session.setAttribute("signup_state",1);
        return "redirect:/home";
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
                List<Comic> list2 =comicRepository.findBySeriesid(seriesid);
                session.setAttribute("comics",list2);
                return "series";
            }
        }
        session.setAttribute("state", 2);
        session.setAttribute("series",null);
        return "series";
    }


    @RequestMapping("/read")
    public String pageinfo(HttpServletRequest request) {
        Iterable<Series> serieslist = seriesRepository.findAll();
        HttpSession session = request.getSession();
        if (session.getAttribute("Name") == null) {
            return "redirect:/login";
        }

        Iterable<Page> a = pageRepository.findAll();
        Iterable<Comic> b = comicRepository.findAll();
        int seriesid=0;
        int comicindex=0;
        int previouschapterid=-1;
        int nextchapterid=-1;
        int comicid = Integer.parseInt(request.getParameter("id"));
        int pageindex = Integer.parseInt(request.getParameter("index"));

        for(Comic c:b){
            if(c.getId().equals(comicid)) {
                seriesid = c.getSeriesid();
                comicindex=c.getIndexs();
                break;
            }
        }

        for (Series s : serieslist) {
            if (s.getId().equals(seriesid)) {
                session.setAttribute("series",s);
            }
        }
        List<Page> list2 = pageRepository.findByComicid(comicid);
        List<Comic> list3 =comicRepository.findBySeriesid(seriesid);

        Boolean aim1=false;
        Boolean aim2=false;

        for(Comic c:list3){
            if(aim1 && aim2){
                break;
            }
            if(c.getIndexs().equals(comicindex-1)) {
                previouschapterid = c.getId();
                aim1=true;
            }
            if(c.getIndexs().equals(comicindex+1)) {
                nextchapterid = c.getId();
                aim2=true;
            }
        }
        session.setAttribute("chapternumber",list3.size());
        session.setAttribute("comicindex",comicindex);
        session.setAttribute("pagenumber",list2.size());
        session.setAttribute("nextchapterid", nextchapterid);
        session.setAttribute("previouschapterid",previouschapterid );
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

    @GetMapping("/administrator")
    public String administrator() {
        return "administrator";
    }


    @GetMapping("/author")
    public String author() {
        return "author";
    }
    @GetMapping("/category")
    public String category() {
        return "category";
    }
    @GetMapping("/createComic")
    public String createComic() {
        return "createComic";
    }
    @GetMapping("/drawComic")
    public String drawComic() {
        return "drawComic";
    }
    @GetMapping("/editInformation")
    public String editInformation() {
        return "editInformation";
    }
    @GetMapping("/favorite")
    public String favorite() {
        return "favorite";
    }
    @GetMapping("/follower")
    public String follower() {
        return "follower";
    }
    @GetMapping("/history")
    public String history() {
        return "history";
    }

    @GetMapping("/manageAuthor")
    public String manageAuthor() {
        return "manageAuthor";
    }
    @GetMapping("/message")
    public String message() {
        return "message";
    }
    @GetMapping("/personal")
    public String personal() {
        return "personal";
    }
    @GetMapping("/search")
    public String search() {
        return "search";
    }
    @GetMapping("/subscription")
    public String subscription() {
        return "subscription";
    }
    @GetMapping("/viewChapters")
    public String viewChapters() {
        return "viewChapters";
    }
    @RequestMapping("/signout")
    public String signout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/login";
    }
}
