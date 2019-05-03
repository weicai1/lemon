package com.example.Lemon;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.net.www.http.HttpClient;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.rowset.serial.SerialBlob;
import javax.swing.text.html.Option;
import javax.xml.bind.DatatypeConverter;


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
    @Autowired
    private SubscribedcomicRepository subscribedcomicRepository;
    @Autowired
    private SubscribedauthorRepository subscribedauthorRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ComiccommentRepository comiccommentRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private SeriescommentRepository seriescommentRepository;
    @Autowired
    private ViewcomicRepository viewcomicRepository;



    @GetMapping("/")
    public String aaa(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") != null) {
            return "redirect:/home";
        }
        return "signIn";
    }


    @GetMapping("/login")
    public String asd(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") != null) {
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
                session.setAttribute("userid",u.getId());
                session.setAttribute("login_state",1);
                if(u.getIssuperuser()){
                    return "redirect:/administrator";
                }
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
        session.setAttribute("userid", user.getId());
        session.setAttribute("signup_state",1);
        return "redirect:/home";
    }


    @RequestMapping("/home")
    public String home(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        ArrayList<Subscribedcomic> scl = (ArrayList<Subscribedcomic>)subscribedcomicRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));

        ArrayList<Integer> subscribeComicList=new ArrayList<Integer>();
        for(int i=0;i<scl.size();i++){
            subscribeComicList.add(scl.get(i).getSeriesid());
        }
        session.setAttribute("subscribeComicList",subscribeComicList);
        ArrayList<Subscribedauthor> sal = (ArrayList<Subscribedauthor>)subscribedauthorRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));

        ArrayList<Integer> subscribeAuthorList=new ArrayList<Integer>();
        for(int i=0;i<sal.size();i++){
            subscribeAuthorList.add(sal.get(i).getAuthorid());
        }
        session.setAttribute("subscribeAuthorList",subscribeAuthorList);
        Iterable<Series> alls = seriesRepository.findAll();

        ArrayList<Series> sorts = new ArrayList<>();
        for (Series s : alls) {
            sorts.add(s);
        }
        for (int i = 0;i<sorts.size()-1;i++){
            for(int j = i+1;j<sorts.size();j++){
                if(sorts.get(i).getSubnumber()<sorts.get(j).getSubnumber()){
                    Series temp = sorts.get(i);
                    sorts.set(i,sorts.get(j));
                    sorts.set(j,temp);
                }
            }
        }
        ArrayList<String> al = new ArrayList<String>();
        int count=0;
        for(Series se:sorts){
            if(count>=10){
                break;
            }
            Blob cover=se.getCover();
            String dataurl="";
            if(cover==null){
                dataurl="";
            }
            else{
                dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
            }
            al.add(dataurl);
            se.setCover(null);
            count+=1;
        }
        session.setAttribute("series",sorts);
        session.setAttribute("covers",al);
        int messagenum = getmessage(request);
        System.out.println(messagenum);
        session.setAttribute("messagenum",messagenum);
        return "home";
    }

    @RequestMapping("/series")
    public String seriesinfo(HttpServletRequest request) throws SQLException {
        Iterable<Series> a = seriesRepository.findAll();
        HttpSession session = request.getSession();
        session.setAttribute("seriesname", null);
        session.setAttribute("seriestag", null);
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        int seriesid = Integer.parseInt(request.getParameter("id"));

        for (Series s : a) {
            if (s.getId().equals(seriesid)) {
                session.setAttribute("state", 1);
                session.setAttribute("series", s);
                Blob cover = s.getCover();
                String dataurl = "";
                if (cover == null) {
                    dataurl = "";
                } else {
                    dataurl = "data:image/png;base64," + DatatypeConverter.printBase64Binary(cover.getBytes(1, (int) cover.length()));
                }
                session.setAttribute("seriescover", dataurl);
                List<Comic> list2 = comicRepository.findBySeriesid(seriesid);
                session.setAttribute("comics", list2);
                Iterable<User> user = userRepository.findAll();
                for (User u : user) {
                    if (u.getId() == s.getAuthorid()) {
                        session.setAttribute("seriesname", u.getName());
                    }
                }
                Iterable<Category> category = categoryRepository.findAll();
                String tag = "";
                for (Category c : category) {
                    if (c.getSeriesid() == s.getId()) {
                        tag += c.getTag();
                        tag += ",";
                    }
                }
                if (tag.length() > 0) {
                    tag = tag.substring(0, tag.length() - 1);
                }
                session.setAttribute("seriestag", tag);

                ArrayList<Seriescomment> scs=(ArrayList<Seriescomment>)seriescommentRepository.findBySeriesid(s.getId());
                ArrayList<User> users=new ArrayList<User>();
                ArrayList<String> urls=new ArrayList<String>();

                for(int i=0;i<scs.size();i++){
                    User u = userRepository.findById(scs.get(i).getUserid()).get();
                    Blob pip = u.getProfileimage();
                    String url = "";
                    if (pip == null) {

                    }
                    else {
                        url = "data:image/png;base64," + DatatypeConverter.printBase64Binary(pip.getBytes(1, (int) pip.length()));
                    }
                    users.add(u);
                    urls.add(url);
                    u.setProfileimage(null);

                }
                session.setAttribute("seriescomments", scs);
                session.setAttribute("commentusers", users);
                session.setAttribute("commentimages", urls);
                return "series";
            }
        }
        session.setAttribute("state", 2);
        session.setAttribute("series", null);
        return "series";
    }


    @GetMapping("/administrator")
    public String administrator(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }

        session.setAttribute("adminState",null);

        String inp = request.getParameter("deleteUser");
//        System.out.println(inp);
        if (inp != null) {
            int realid = Integer.parseInt(inp);
            Iterable<Comiccomment> tempcc = comiccommentRepository.findAll();
            for(Comiccomment cc:tempcc){
                if (cc.getUserid() == realid){
                    comiccommentRepository.deleteById(cc.getId());
                }
            }
            Iterable<Favorite> tempfa = favoriteRepository.findAll();
            for(Favorite fa:tempfa){
                if (fa.getUserid() == realid){
                    favoriteRepository.deleteById(fa.getId());
                }
            }
            Iterable<Follow> tempfo = followRepository.findAll();
            for(Follow fo:tempfo){
                if (fo.getFolloweeid() == realid || fo.getFollowerid() == realid){
                    followRepository.deleteById(fo.getId());
                }
            }
            Iterable<History> temphi = historyRepository.findAll();
            for(History hi:temphi){
                if (hi.getUserid() == realid){
                    historyRepository.deleteById(hi.getId());
                }
            }
            Iterable<Series> tempse = seriesRepository.findAll();
            for(Series se:tempse){
                if (se.getAuthorid() == realid){
                    seriesRepository.deleteById(se.getId());
                }
            }
            Iterable<Seriescomment> tempsec = seriescommentRepository.findAll();
            for(Seriescomment sec:tempsec){
                if (sec.getUserid() == realid){
                    seriescommentRepository.deleteById(sec.getId());
                }
            }
            Iterable<Subscribedauthor> tempsa = subscribedauthorRepository.findAll();
            for(Subscribedauthor sa:tempsa){
                if (sa.getUserid() == realid || sa.getAuthorid() == realid){
                    subscribedauthorRepository.deleteById(sa.getId());
                }
            }
            Iterable<Subscribedcomic> tempsc = subscribedcomicRepository.findAll();
            for(Subscribedcomic sc:tempsc){
                if (sc.getUserid() == realid){
                    subscribedcomicRepository.deleteById(sc.getId());
                }
            }
            Iterable<Viewcomic> tempvi = viewcomicRepository.findAll();
            for(Viewcomic vi:tempvi){
                if (vi.getUserid() == realid){
                    viewcomicRepository.deleteById(vi.getId());
                }
            }
            userRepository.deleteById(Integer.parseInt(inp));
            session.setAttribute("adminState",1);
            response.sendRedirect("/administrator");
        }


        List<User> ul = new ArrayList<>();
        Iterable<User> allUser = userRepository.findAll();
        for(User us:allUser){
            if (us.getId() != session.getAttribute("userid")){
                ul.add(us);
            }
        }


        if(ul!=null && ul.size()>0){
            session.setAttribute("adminState",1);

            ArrayList<String> images=new ArrayList<String>();
            ArrayList<String> names =new ArrayList<String>();

            for(User u:ul){
                names.add(u.getName());

                Blob image=u.getProfileimage();
                u.setProfileimage(null);
                String dataurl="";
                if(image==null){
                    dataurl="";
                }
                else{
                    dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(image.getBytes(1,(int)image.length()));
                }
                images.add(dataurl);
            }

            session.setAttribute("images",images);
            session.setAttribute("names",names);
            session.setAttribute("userlist",ul);

            System.out.println(names);

        }
        else {
            session.setAttribute("adminState", 2);
        }
        return "administrator";
    }


    @GetMapping("/author")
    public String author(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("authorState", null);
        session.setAttribute("author",null);
        session.setAttribute("subnumber",null);

        int authorid=Integer.parseInt(request.getParameter("id"));
        Iterable<User> users = userRepository.findAll();
        ArrayList<String> covers = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> subscribes = new ArrayList<String>();
        ArrayList<String> rates = new ArrayList<String>();
        ArrayList<String> categorys = new ArrayList<String>();
        for (User u : users) {
            if (u.getId()==authorid) {
                session.setAttribute("author",u);
                List<Subscribedauthor> sa=subscribedauthorRepository.findByAuthorid(u.getId());
                session.setAttribute("subnumber",sa.size());
                Blob pip=u.getProfileimage();

                String dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(pip.getBytes(1,(int)pip.length()));
                session.setAttribute("profileimagepath",dataurl);
                ArrayList<Series> sl = (ArrayList<Series>)seriesRepository.findByAuthorid(u.getId());
                System.out.println(sl.size());
                for(Series s:sl){
                    Blob page = s.getCover();
                    String dataurl2;
                    if(page==null){
                        //dataurl2="";
                        continue;
                    }
                    else{
                        dataurl2 = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
                    }
                    covers.add(dataurl2);
                    names.add(s.getName());
                    ids.add(s.getId());
                    subscribes.add(s.getSubnumber() + "");
                    if(s.getRate()==null){
                        rates.add("5");
                    }
                    else {
                        rates.add(s.getRate() + "");
                    }
                    Iterable<Category> category = categoryRepository.findAll();
                    String tag = "";
                    for (Category c : category) {
                        if (c.getSeriesid() == s.getId()) {
                            tag += c.getTag();
                            tag += ",";
                        }
                    }
                    if (tag.length() > 0) {
                        tag = tag.substring(0, tag.length() - 1);
                    }
                    categorys.add(tag);
                }
                session.setAttribute("coverlist",covers);
                session.setAttribute("namelist",names);
                session.setAttribute("idlist",ids);
                session.setAttribute("subscribelist", subscribes);
                session.setAttribute("ratelist", rates);
                session.setAttribute("categorylist", categorys);
            }
        }

        return "author";
    }

    @RequestMapping("/category")
    public String category(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }

        session.setAttribute("catState",null);
        String inp = request.getParameter("categoryName");
        String inpsort = request.getParameter("sortBy");
        List<Series> sl = new ArrayList<>();
        Iterable<Category> allcat = categoryRepository.findByTag(inp);

        for(Category ca:allcat){
            sl.add(seriesRepository.getById(ca.getSeriesid()));
        }

        Collections.sort(sl, Series.SeriesSubscriptionComparator);
        if (inpsort != null){
            if (inpsort.equals("updateTime")){
                Collections.sort(sl, Series.SeriesIdComparator);
            }
            else if (inpsort.equals("subscription")) {
                Collections.sort(sl, Series.SeriesSubscriptionComparator);
            }
            else if (inpsort.equals("rate")){
                Collections.sort(sl, Series.SeriesRateComparator);
            }
        }
        if(sl!=null && sl.size()>0){
            session.setAttribute("catState",1);
            ArrayList<String> covers=new ArrayList<String>();
            ArrayList<String> names =new ArrayList<String>();
            ArrayList<String> authors =new ArrayList<String>();
            ArrayList<String> subscribes =new ArrayList<String>();
            ArrayList<String> rates =new ArrayList<String>();
            ArrayList<String> categorys =new ArrayList<String>();
            int count=0;
            for(Series s:sl){
                if(count>=10){
                    break;
                }
                names.add(s.getName());
                User u=userRepository.findById(s.getAuthorid()).get();
                authors.add(u.getName());
                subscribes.add(s.getSubnumber()+"");
                rates.add(s.getRate()+"");
                Iterable<Category> category = categoryRepository.findAll();
                String tag="";
                for(Category c:category){
                    if(c.getSeriesid()==s.getId()){
                        tag+=c.getTag();
                        tag+=",";
                    }
                }
                if (tag.length() > 0) {
                    tag = tag.substring(0, tag.length() - 1);
                }
                categorys.add(tag);
                Blob cover=s.getCover();
                s.setCover(null);
                String dataurl="";
                if(cover==null){
                    dataurl="";
                }
                else{
                    dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
                }
                covers.add(dataurl);
                count+=1;
            }
            session.setAttribute("covers",covers);
            session.setAttribute("names",names);
            session.setAttribute("authors",authors);
            session.setAttribute("subscribes",subscribes);
            session.setAttribute("rates",rates);
            session.setAttribute("categorys",categorys);
            session.setAttribute("catlist",sl);

        }


        else{
            session.setAttribute("catState",2);
        }

        return "category";
    }
    @RequestMapping("/createComic")
    public String createComic(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("createState",null);
        session.setAttribute("drawSeries", null);
        session.setAttribute("drawnumber", null);
        session.setAttribute("drawcomic", null);
        session.setAttribute("Pindex",null);
        session.setAttribute("seriesname",null);
        return "createComic";
    }
    @GetMapping("/drawComic")
    public String newComic(HttpServletRequest request) throws IOException, SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String sn = request.getParameter("seriesName");
        if(session.getAttribute("createState")==null||(Integer.parseInt(session.getAttribute("createState")+"")!=0)) {


            Iterable<Series> is = seriesRepository.findAll();
            for (Series s : is) {
                if (s.getName().equals(sn)) {
                    session.setAttribute("createState", 1);
                    return "createComic";
                }
            }
            Series ser = new Series();
            ser.setName(sn);
            ser.setAuthorid(Integer.parseInt(session.getAttribute("userid") + ""));
            ser.setChapternumber(1);
            ser.setRate(4.0);
            ser.setSubnumber(0);
            ser.setDescription("Funny");
            //File defaultcover= new File("src/main/resources/static/images/Lemon-Man-1.png");
            //FileInputStream fin = new FileInputStream(defaultcover);
            //ser.setCover();

            //byte[] fileContent = FileUtils.readFileToByteArray(new File("/src/main/resources/static/images/Lemon-Man-1.png"));
            //Blob blob = new SerialBlob(fileContent);
            //ser.setCover(blob);

            seriesRepository.save(ser);
            String[] tags=request.getParameter("tags").split(",");
            for(String s : tags){
                Category cat=new Category();
                cat.setSeriesid(ser.getId());
                cat.setTag(s);
                categoryRepository.save(cat);
            }

            Comic cm = new Comic();
            cm.setSeriesid(ser.getId());
            cm.setIndex(1);
            comicRepository.save(cm);
            session.setAttribute("createState", 0);
            session.setAttribute("drawSeries", ser);
            session.setAttribute("drawnumber", 1);
            session.setAttribute("drawcomic", cm);
            session.setAttribute("Pindex",1);
            session.setAttribute("seriesname",sn);
        }
        else{
            String index = request.getParameter("index");
            if(index==null || session.getAttribute("seriesname")==null){
                return "redirect:/createComic";
            }
            session.setAttribute("Pindex",index);
        }
        return "drawComic";
    }
    @PostMapping("/drawComic")
    public String drawComic2(HttpServletRequest request) throws IOException, SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String mission = request.getParameter("mission");
        System.out.println(mission);
        if(mission!=null && mission.equals("1")){
            String seriesName=""+session.getAttribute("seriesname");
            session.setAttribute("drawnumber", Integer.parseInt(""+session.getAttribute("drawnumber"))+1);
            session.setAttribute("Pindex",Integer.parseInt(""+session.getAttribute("drawnumber")));
            return "redirect:/drawComic.html?seriesName="+seriesName+"&index="+session.getAttribute("Pindex");
        }
        if(mission!=null && mission.equals("2")){
            String seriesName=""+session.getAttribute("seriesname");

            session.setAttribute("drawnumber", Integer.parseInt(""+session.getAttribute("drawnumber"))-1);
            session.setAttribute("Pindex",Integer.parseInt(""+session.getAttribute("drawnumber")));
            return "redirect:/drawComic.html?seriesName="+seriesName+"&index="+session.getAttribute("Pindex");
        }
        String m=request.getParameter("data");
        String paindex= ""+session.getAttribute("Pindex");
        if(m!=null){
            byte[] imagedata = DatatypeConverter.parseBase64Binary(m.substring(m.indexOf(",") + 1));
            Blob blob = new SerialBlob(imagedata);
            Page p = new Page();
            p.setContent(blob);
            int i2= Integer.parseInt(paindex);
            int c2= ((Comic)session.getAttribute("drawcomic")).getId();
            p.setIndexs(i2);
            p.setComicid(c2);
            if(i2==1){
                Series temp = seriesRepository.findByName(""+session.getAttribute("seriesname")).get(0);
                temp.setCover(blob);
                long unixTime = System.currentTimeMillis() / 1000L;
                temp.setUpdatetime(unixTime);
                seriesRepository.save(temp);
            }
            pageRepository.save(p);
        }

        return "drawComic";
    }

    @GetMapping("/editInformation")
    public String editInformation(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        session.setAttribute("editstate",null);
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        User u = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();
        //String pip=u.getProfileimage();


        Blob pip=u.getProfileimage();
        String dataurl="";
        if(pip==null){
            //pip="templates/default.png";
        }
        //change pip to base64
        else{
            dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(pip.getBytes(1,(int)pip.length()));
        }

        u.setProfileimage(null);
        session.setAttribute("currentuser",u);
        session.setAttribute("profileimagepath",dataurl);
        return "editInformation";
    }

    @PostMapping("/editInformation")
    public String editInformation2(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException, SQLException {

        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("editstate",null);
       /* String fileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        fileName = "src/main/resources/templates/profileimage/"+uuid+suffix;
        File a =new File(fileName);
        if(!a.exists()) {
            try {
                a.createNewFile();
                FileOutputStream  fos = new FileOutputStream(a);
                fos.write(file.getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    //    String name= request.getParameter("name");
        String bio =request.getParameter("bio");


    //    Optional check = userRepository.findByName(name);

        User u = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();



        // if this new id is already used by others
     //   if (!name.equals(u.getName()) && check!= Optional.empty()){
     //       session.setAttribute("editstate",2);
     //       return "editInformation";
     //   }


        String temp=DatatypeConverter.printBase64Binary(file.getBytes());

        if(!temp.equals("")) {
            String dataurl = "data:image/png;base64," + temp;
            session.setAttribute("profileimagepath", dataurl);
            //u.setProfileimage("profileimage/"+uuid+suffix);
            Blob blob = new SerialBlob(file.getBytes());
            u.setProfileimage(blob);
        }
    //    u.setName(name);
        u.setSignature(bio);
        userRepository.save(u);
        session.setAttribute("currentuser",u);

        //StringBuilder sb = new StringBuilder();
        //sb.append("data:image/png;base64,");
        //sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(file.getBytes(), false)));
        //contourChart = sb.toString();
        session.setAttribute("editstate",1);
        return "editInformation";
    }
    @GetMapping("/favorite")
    public String favorite() {
        return "favorite";
    }
    @GetMapping("/follower")
    public String follower(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        ArrayList<Subscribedauthor> sbl = (ArrayList<Subscribedauthor>)subscribedauthorRepository.findByAuthorid(Integer.parseInt(""+session.getAttribute("userid")));
        ArrayList<User> followerList=new ArrayList<User>();
        ArrayList<String> imageList= new ArrayList<>();
        ArrayList<String> followernameList = new ArrayList<>();
        for(int i=0;i<sbl.size();i++){
            User u = userRepository.findById(sbl.get(i).getUserid()).get();
            followerList.add(u);
            followernameList.add(u.getName());
            Blob page = u.getProfileimage();
            String dataurl;
            if(page==null){
                dataurl="";
            }
            else{
                dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
            }
            imageList.add(dataurl);
            //u.setProfileimage(null);
        }
        System.out.println("size "+followerList.size());
        System.out.println("BBBBBBBBBBB");
        for (int i = 0;i<followerList.size();i++){
            System.out.println(followerList.get(i).getName());
        }
        session.setAttribute("followerList",followerList);
        session.setAttribute("followernamelist",followernameList);
        session.setAttribute("authorProfile",imageList);
        return "follower";
    }


    @GetMapping("/history")
    public String history(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("historyState",null);
        session.setAttribute("historycovers",null);
        session.setAttribute("historynames",null);
        session.setAttribute("historychapters",null);
        session.setAttribute("historypages",null);
        session.setAttribute("historyserieslist",null);
        ArrayList<History> sl = new ArrayList<>();
        Iterable<History> alls = historyRepository.findAll();
        for(History h:alls){
            sl.add(h);
        }
        if(sl!=null && sl.size()>0){
            for (int i = 0;i<sl.size()-1;i++){
                for(int j = i+1;j<sl.size();j++){
                    if(sl.get(i).getId()<sl.get(j).getId()){
                        History temp = sl.get(i);
                        sl.set(i,sl.get(j));
                        sl.set(j,temp);
                    }
                }
            }

            session.setAttribute("historyState",1);


            ArrayList<String> covers=new ArrayList<String>();
            ArrayList<String> names=new ArrayList<String>();
            ArrayList<Integer> chapters=new ArrayList<Integer>();
            ArrayList<Integer> pages=new ArrayList<Integer>();
            int count=0;
            for(History history:sl){
                if(count>=10){
                    break;
                }
                Series s=seriesRepository.findById(history.getSeriesid()).get();
                names.add(s.getName());
                chapters.add(history.getComicid());
                pages.add(history.getPageindex());
                Blob cover=s.getCover();
                s.setCover(null);
                String dataurl="";
                if(cover==null){
                    dataurl="";
                }
                else{
                    dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
                }
                covers.add(dataurl);
                count+=1;
            }
            session.setAttribute("historycovers",covers);
            session.setAttribute("historynames",names);
            session.setAttribute("historychapters",chapters);
            session.setAttribute("historypages",pages);
            session.setAttribute("historyserieslist",sl);
        }
        else{
            session.setAttribute("historyState",2);
            //empty
        }

        return "history";
    }

    @GetMapping("/manageAuthor")
    public String manageAuthor(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
//        session.setAttribute("userid", 4);

        session.setAttribute("manageState",null);

        String inp = request.getParameter("User");

        System.out.println(inp);

        String inpcomic = request.getParameter("deletecomic");
        String inpseries = request.getParameter("deleteseries");

        if (inpcomic != null) {
            int realcomicid = Integer.parseInt(inpcomic);
            Comic c =comicRepository.getById(realcomicid);
            Series temps =seriesRepository.getById(c.getSeriesid());
            temps.setChapternumber(temps.getChapternumber()- 1);
            seriesRepository.save(temps);
            Iterable<Page> temppa = pageRepository.findByComicid(realcomicid);
            for (Page pa:temppa){
                pageRepository.deleteById(pa.getId());
            }
            comicRepository.deleteById(realcomicid);
            session.setAttribute("manageState",1);
            response.sendRedirect("/manageAuthor?User="+inp);
        }
        if (inpseries != null) {
            int realseriesid = Integer.parseInt(inpseries);

            Iterable<Page> temppa = pageRepository.findAll();
            Iterable<Comic> tempco = comicRepository.findBySeriesid(realseriesid);
            List<Integer> coid = new ArrayList<>();

            for (Comic co : tempco){
                coid.add(co.getId());
            }
            for (Page pa:temppa){
                if (coid.contains(pa.getComicid())) {
                    pageRepository.deleteById(pa.getId());
                }
            }
            for (Comic co:tempco){
                comicRepository.deleteById(co.getId());
            }
            seriesRepository.deleteById(realseriesid);
            session.setAttribute("manageState",1);
            response.sendRedirect("/manageAuthor?User="+inp);
        }


        Map<String, ArrayList<String>> AllSeriesname = new HashMap<>();
        Map<String, ArrayList<String>> AllSeriesid = new HashMap<>();

        Iterable<Series> sli = seriesRepository.findByAuthorid(Integer.parseInt(inp));
        List<Series> sllist = new ArrayList<>();
        for(Series us:sli){
            sllist.add(us);
        }


        if(sllist!=null && sllist.size()>0){
            session.setAttribute("manageState",1);

            for (Series s:sllist){
                s.setCover(null);

                List<Comic> cl = comicRepository.findBySeriesid(s.getId());

                ArrayList<String> comicids=new ArrayList<String>();
                ArrayList<String> comicnames =new ArrayList<String>();

                for (Comic c:cl){
                    comicnames.add(Integer.toString(c.getIndexs()));
                    comicids.add(Integer.toString(c.getId()));
                }

                AllSeriesname.put(s.getName(), comicnames);
                AllSeriesid.put(s.getName(), comicids);
            }


            System.out.println(AllSeriesname.keySet());
            System.out.println(AllSeriesid.keySet());
            System.out.println(sllist);
            session.setAttribute("AllSeriesname",AllSeriesname);
            session.setAttribute("AllSeriesid",AllSeriesid);
            session.setAttribute("serieslist", sllist);

        }
        else {
            session.setAttribute("manageState", 2);
        }

        return "manageAuthor";
    }






    @RequestMapping("/message")
    public String message(HttpServletRequest request) throws SQLException{
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        User u = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();
        System.out.println(u.getName());
        System.out.println(u.getLastViewTime());
        if(u.getLastViewTime()==null){
            u.setLastViewTime((long) 0);
        }
        ArrayList<Subscribedauthor> authorlist = (ArrayList<Subscribedauthor>)subscribedauthorRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
        //ArrayList<Integer> sublist = new ArrayList<>();
        ArrayList<Series> allseries = new ArrayList<>();
        System.out.println("authorlist size "+authorlist.size());
        for(int i = 0;i<authorlist.size();i++){
            Integer id = authorlist.get(i).getAuthorid();//find an author user subscribed
            //find all series that author updated after user viewed
            ArrayList<Series> slist2 = (ArrayList<Series>)seriesRepository.findByAuthoridAndUpdatetimeAfter(id,u.getLastViewTime());
            System.out.println("size "+slist2.size());
            allseries.addAll(slist2);
        }
        long unixTime = System.currentTimeMillis() / 1000L;
        u.setLastViewTime(unixTime);
        userRepository.save(u);
        System.out.println("------------------");
        System.out.println(allseries.size());
        ArrayList<String> coverlist = new ArrayList<>();
        ArrayList<String> namelist = new ArrayList<>();
        ArrayList<Long> timelist = new ArrayList<>();
        ArrayList<Integer> idlist = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        for (Series s:allseries){
            System.out.println(s.getName());
            namelist.add(s.getName());
            Blob page = s.getCover();
            String dataurl;
            if(page==null){
                dataurl="";
            }
            else{
                dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
            }
            coverlist.add(dataurl);
            timelist.add(s.getUpdatetime());
            idlist.add(s.getId());

            User us = userRepository.findById(s.getAuthorid()).get();
            authors.add(us.getName());




        }
        session.setAttribute("timelist",timelist);
        session.setAttribute("coverlist",coverlist);
        session.setAttribute("namelist",namelist);
        session.setAttribute("idlist",idlist);
        session.setAttribute("authorlist", authors);
        return "message";

    }


    public int getmessage(HttpServletRequest request) throws SQLException{
        HttpSession session = request.getSession();
        User u = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();
        System.out.println(u.getName());
        System.out.println(u.getLastViewTime());
        if(u.getLastViewTime()==null){
            u.setLastViewTime((long) 0);
        }
        ArrayList<Subscribedauthor> authorlist = (ArrayList<Subscribedauthor>)subscribedauthorRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
        //ArrayList<Integer> sublist = new ArrayList<>();
        ArrayList<Series> allseries = new ArrayList<>();
        System.out.println("authorlist size "+authorlist.size());
        for(int i = 0;i<authorlist.size();i++){
            Integer id = authorlist.get(i).getAuthorid();//find an author user subscribed
            //find all series that author updated after user viewed
            ArrayList<Series> slist2 = (ArrayList<Series>)seriesRepository.findByAuthoridAndUpdatetimeAfter(id,u.getLastViewTime());
            System.out.println("size "+slist2.size());
            allseries.addAll(slist2);
        }
        System.out.println("------------------");
        System.out.println(allseries.size());
        return allseries.size();


    }


    @GetMapping("/personal")
    public String personal(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        List<Series> works = seriesRepository.findByAuthorid(Integer.parseInt(""+session.getAttribute("userid")));
        User user = userRepository.findById(Integer.parseInt((""+session.getAttribute("userid")))).get();
        Blob userimage = user.getProfileimage();
        String dataurl="";
        if(userimage==null){
            dataurl="";
        }
        else{
            dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(userimage.getBytes(1,(int)userimage.length()));
        }
        user.setProfileimage(null);
        ArrayList<Series> workslist =new ArrayList<Series>();

        ArrayList<String> covers=new ArrayList<String>();


        for(Series s : works){
            Blob cover=s.getCover();
            s.setCover(null);
            workslist.add(s);
            String dataurl2="";
            if(cover==null){
                dataurl2="";
            }
            else{
                dataurl2 = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
            }
            covers.add(dataurl2);
        }



        session.setAttribute("personalcovers",covers);
        session.setAttribute("profileimagepath",dataurl);
        session.setAttribute("currentuser",user);
        session.setAttribute("works",workslist);

        return "personal";
    }
    @RequestMapping("/search")
    public String search(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("searchState",null);
        String keyword = request.getParameter("keyword").toLowerCase();
        String[]keywords = keyword.split(" ");
        List<Series> sl = new ArrayList<>();
        ArrayList<Integer> allseriesId = new ArrayList<>();
        Iterable<Series> alls = seriesRepository.findAll();
        for(int i = 0;i<keywords.length;i++){
            for(Series s:alls){
                if(s.getName().toLowerCase().contains(keywords[i])){
                    if(!allseriesId.contains(s.getId())){
                        sl.add(s);
                        allseriesId.add(s.getId());
                    }
                }
            }
        }
        if(sl!=null && sl.size()>0){
            session.setAttribute("searchState",1);


            ArrayList<String> covers=new ArrayList<String>();
            for(Series s:sl){
                Blob cover=s.getCover();
                s.setCover(null);
                String dataurl="";
                if(cover==null){
                    dataurl="";
                }
                else{
                    dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
                }
                covers.add(dataurl);
            }
            session.setAttribute("covers",covers);
            session.setAttribute("serieslist",sl);

        }
        else{
            session.setAttribute("searchState",2);
            //empty
        }

        return "search";
    }

    @RequestMapping("/viewChapters")
    public String viewChapters(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String del=request.getParameter("delete");
        int seriesid=Integer.parseInt(request.getParameter("id"));
        Series s = seriesRepository.findById(seriesid).get();
        if(s.getAuthorid()!=Integer.parseInt(session.getAttribute("userid")+"")){
            return "redirect:/home";
        }
        if(del!=null && del.equals("1")){

        }
        Blob cover=s.getCover();
        session.setAttribute("tempcover",cover);
        s.setCover(null);
        String dataurl="";
        if(cover==null){
            dataurl="";
        }
        else{
            dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
        }

        int cn = s.getChapternumber();
        List<Comic> d = comicRepository.findBySeriesid(seriesid);
        ArrayList<Comic> chapters=new ArrayList<Comic>();
        for (int i=0;i<cn;i++){
            for (int j=0;j<cn;j++){
                if(d.get(j).getIndexs()==i+1){
                    chapters.add(d.get(j));
                }
            }
        }
        ArrayList<String> Chpatercover=new ArrayList<String>();
        for(Comic c:chapters){
            int cid=c.getId();
            Page p =pageRepository.findByComicidAndIndexs(cid,1).get(0);
            Blob ccover = p.getContent();
            String curl="";
            if(ccover==null){
                curl="";
            }
            else{
                curl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(ccover.getBytes(1,(int)ccover.length()));
            }
            Chpatercover.add(curl);
        }

        session.setAttribute("ccover",Chpatercover);
        session.setAttribute("chapters",chapters);
        session.setAttribute("cover",dataurl);
        session.setAttribute("currentseries",s);

        return "viewChapters";
    }
    @GetMapping("/addComic")
    public String addChapter(HttpServletRequest request){
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }

        Series s =(Series)session.getAttribute("currentseries");

        String chapterindex=request.getParameter("chapterindex");
        if(s==null || s.getAuthorid()!=Integer.parseInt(""+session.getAttribute("userid"))){
            return "redirect:/home";
        }

        String create = request.getParameter("create");
        if(create!=null && create.equals("1")){

            //session.setAttribute("Pindex", index);
            s.setChapternumber(s.getChapternumber() + 1);
            s.setCover((Blob) session.getAttribute("tempcover"));
            seriesRepository.save(s);
            //s.setCover(null);

            Comic cm = new Comic();
            cm.setSeriesid(s.getId());
            cm.setIndex(Integer.parseInt(chapterindex));
            comicRepository.save(cm);
            session.setAttribute("drawSeries", s);
            session.setAttribute("drawnumber", 1);
            session.setAttribute("drawcomic", cm);
            session.setAttribute("Pindex", 1);
            session.setAttribute("seriesname", s.getName());

            return "addComic";

        }
        else {

                String index = request.getParameter("index");
                if(index==null || session.getAttribute("seriesname")==null){
                    return "redirect:/home";
                }
                session.setAttribute("Pindex",index);
                return "addComic";




        }
    }


    @PostMapping("/addComic")
    public String addComic2(HttpServletRequest request) throws IOException, SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String mission = request.getParameter("mission");
        System.out.println(mission);
        if(mission!=null && mission.equals("1")){
            String seriesName=""+session.getAttribute("seriesname");
            session.setAttribute("drawnumber", Integer.parseInt(""+session.getAttribute("drawnumber"))+1);
            session.setAttribute("Pindex",Integer.parseInt(""+session.getAttribute("drawnumber")));
            return "redirect:/addComic?index="+session.getAttribute("drawnumber");
        }
        if(mission!=null && mission.equals("2")){
            String seriesName=""+session.getAttribute("seriesname");
            session.setAttribute("drawnumber", Integer.parseInt(""+session.getAttribute("drawnumber"))-1);
            session.setAttribute("Pindex",Integer.parseInt(""+session.getAttribute("drawnumber")));
            return "redirect:/addComic?index="+session.getAttribute("drawnumber");
        }
        String m=request.getParameter("data");
        String paindex= ""+session.getAttribute("Pindex");
        if(m!=null){
            byte[] imagedata = DatatypeConverter.parseBase64Binary(m.substring(m.indexOf(",") + 1));
            Blob blob = new SerialBlob(imagedata);
            Page p = new Page();
            p.setContent(blob);
            int i2= Integer.parseInt(paindex);
            int c2= ((Comic)session.getAttribute("drawcomic")).getId();
            p.setIndexs(i2);
            p.setComicid(c2);
            pageRepository.save(p);
        }

        return "addComic";
    }


    @RequestMapping("/signout")
    public String signout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/login";
    }

    @RequestMapping("/toSubscribe")
    public String toSubscribe(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        int subscriber = (Integer)session.getAttribute("userid");
        int subscribedSeries = Integer.parseInt(request.getParameter("seriesid")+"");
        String issub=request.getParameter("subscribe");
        if(issub.equals("Unsubscribe")){
            List<Subscribedcomic> scl = subscribedcomicRepository.findBySeriesidAndUserid(subscribedSeries,subscriber);
            if(scl.size()!=0){
                List<Subscribedcomic> sc = subscribedcomicRepository.findBySeriesidAndUserid(subscribedSeries,subscriber);
                subscribedcomicRepository.deleteById(sc.get(0).getId());
                Series seri=seriesRepository.findById(subscribedSeries).get();
                seri.setSubnumber(seri.getSubnumber()-1);
                seriesRepository.save(seri);
                ArrayList<Subscribedcomic> sbl = (ArrayList<Subscribedcomic>)subscribedcomicRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
                ArrayList<Integer> subscribeComicList=new ArrayList<Integer>();
                for(int i=0;i<sbl.size();i++){
                    subscribeComicList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("subscribeComicList",subscribeComicList);

                return "series";

            }
            else{
                return "series";
            }
        }
        else {
            List<Subscribedcomic> scl = subscribedcomicRepository.findBySeriesidAndUserid(subscribedSeries,subscriber);
            if(scl.size()==0){
                Subscribedcomic sc = new Subscribedcomic();
                sc.setSeriesid(subscribedSeries);
                sc.setUserid(subscriber);
                subscribedcomicRepository.save(sc);
                ArrayList<Subscribedcomic> sbl = (ArrayList<Subscribedcomic>) subscribedcomicRepository.findByUserid(Integer.parseInt("" + session.getAttribute("userid")));
                ArrayList<Integer> subscribeComicList = new ArrayList<Integer>();
                for (int i = 0; i < sbl.size(); i++) {
                    subscribeComicList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("subscribeComicList", subscribeComicList);
                Series seri = seriesRepository.findById(subscribedSeries).get();
                seri.setSubnumber(seri.getSubnumber() + 1);
                seriesRepository.save(seri);
                return "series";
            }
            else{
                return "series";
            }

        }
    }



    @RequestMapping("/toSubscribeAuthor")
    public String toSubscribeAuthor(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        int subscriber = (Integer)session.getAttribute("userid");
        int subscribedAuthor = Integer.parseInt(request.getParameter("authorid")+"");
        String issub=request.getParameter("subscribe");
        if(issub.equals("Unsubscribe")) {
            List<Subscribedauthor> sal = subscribedauthorRepository.findByAuthoridAndUserid(subscribedAuthor,subscriber);
            if (sal.size() != 0) {
                subscribedauthorRepository.deleteById(sal.get(0).getId());
                User us = userRepository.findById(subscribedAuthor).get();
                ArrayList<Subscribedauthor> sbl = (ArrayList<Subscribedauthor>) subscribedauthorRepository.findByUserid(Integer.parseInt("" + session.getAttribute("userid")));
                ArrayList<Integer> subscribeComicList = new ArrayList<Integer>();
                for (int i = 0; i < sbl.size(); i++) {
                    subscribeComicList.add(sbl.get(i).getAuthorid());
                }
                session.setAttribute("subscribeAuthorList", subscribeComicList);

                return "author";

            }
            else{
                return "author";
            }
        }
        else {
            List<Subscribedauthor> sal = subscribedauthorRepository.findByAuthoridAndUserid(subscribedAuthor,subscriber);
            if (sal.size() == 0) {
                Subscribedauthor sa = new Subscribedauthor();
                sa.setAuthorid(subscribedAuthor);
                sa.setUserid(subscriber);
                subscribedauthorRepository.save(sa);
                ArrayList<Subscribedauthor> sbl = (ArrayList<Subscribedauthor>) subscribedauthorRepository.findByUserid(Integer.parseInt("" + session.getAttribute("userid")));
                ArrayList<Integer> subscribeAuthorList = new ArrayList<Integer>();
                for (int i = 0; i < sbl.size(); i++) {
                    subscribeAuthorList.add(sbl.get(i).getAuthorid());
                }
                session.setAttribute("subscribeAuthorList", subscribeAuthorList);
                User us = userRepository.findById(subscriber).get();
                return "author";
            }
            else{
                return "author";
            }
        }
    }


    @RequestMapping("/like")
    public String like(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        session.setAttribute("likeitem",null);
        int liker = (Integer)session.getAttribute("userid");
        int likedSeries = Integer.parseInt(request.getParameter("seriesid")+"");
        String likeState=request.getParameter("state");

        if(likeState.equals("dislike")){
            List<Favorite> scl = favoriteRepository.findBySeriesidAndUserid(likedSeries,liker);
            System.out.println(scl);
            if(scl.size()!=0){
                Favorite f=favoriteRepository.findBySeriesidAndUserid(likedSeries,liker).get(0);
                f.setLikestate("dislike");
                favoriteRepository.save(f);
                ArrayList<Favorite> sbl = (ArrayList<Favorite>)favoriteRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
                ArrayList<Integer> likeList=new ArrayList<Integer>();
                for(int i=0;i<sbl.size();i++){
                    likeList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("likeList",likeList);
                session.setAttribute("likeitem",scl.get(0).getLikestate());
                return "series";
            }
            else{
                Favorite favorite =new Favorite();
                favorite.setSeriesid(likedSeries);
                favorite.setUserid(liker);
                favorite.setLikestate("dislike");
                favoriteRepository.save(favorite);
                ArrayList<Favorite> sbl = (ArrayList<Favorite>)favoriteRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
                ArrayList<Integer> likeList=new ArrayList<Integer>();
                for(int i=0;i<sbl.size();i++){
                    likeList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("likeList",likeList);
                session.setAttribute("likeitem", favorite.getLikestate());
                return "series";
            }
        }
        else {
            List<Favorite> scl = favoriteRepository.findBySeriesidAndUserid(likedSeries,liker);
            if(scl.size()!=0){
                Favorite f=favoriteRepository.findBySeriesidAndUserid(likedSeries,liker).get(0);
                f.setLikestate("like");
                favoriteRepository.save(f);
                ArrayList<Favorite> sbl = (ArrayList<Favorite>)favoriteRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
                ArrayList<Integer> likeList=new ArrayList<Integer>();
                for(int i=0;i<sbl.size();i++){
                    likeList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("likeList",likeList);
                session.setAttribute("likeitem",scl.get(0).getLikestate());
                return "series";
            }
            else{
                Favorite favorite =new Favorite();
                favorite.setSeriesid(likedSeries);
                favorite.setUserid(liker);
                favorite.setLikestate("like");
                favoriteRepository.save(favorite);
                ArrayList<Favorite> sbl = (ArrayList<Favorite>)favoriteRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
                ArrayList<Integer> likeList=new ArrayList<Integer>();
                for(int i=0;i<sbl.size();i++){
                    likeList.add(sbl.get(i).getSeriesid());
                }
                session.setAttribute("likeList",likeList);
                session.setAttribute("likeitem", favorite.getLikestate());
                return "series";
            }

        }
    }


    @RequestMapping("/subscription")
    public String subscription(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        User loginuser = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();
        ArrayList<Subscribedauthor> sbl = (ArrayList<Subscribedauthor>)subscribedauthorRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
        ArrayList<User> subscribeAuthorList=new ArrayList<User>();
        ArrayList<String> imageList= new ArrayList<>();
        for(int i=0;i<sbl.size();i++){
            User u = userRepository.findById(sbl.get(i).getAuthorid()).get();
            subscribeAuthorList.add(u);
            Blob page = u.getProfileimage();
            String dataurl;
            if(page==null){
                dataurl="";
            }
            else{
                dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
            }
            imageList.add(dataurl);
            u.setProfileimage(null);
        }
        System.out.println("BBBBBBBBBBB");
        System.out.println(subscribeAuthorList);
        session.setAttribute("subscribeAuthorList",subscribeAuthorList);
        session.setAttribute("authorProfile",imageList);
        ArrayList<Subscribedcomic> scl = (ArrayList<Subscribedcomic>)subscribedcomicRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
        ArrayList<Series> subscribecomicList=new ArrayList<Series>();
        ArrayList<String> coverList = new ArrayList<>();
        for(int i=0;i<scl.size();i++){
            Series s = seriesRepository.findById(scl.get(i).getSeriesid()).get();
            subscribecomicList.add(s);
            Blob page = s.getCover();
            String dataurl;
            if(page==null){
                dataurl="";
            }
            else{
                dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
            }
            coverList.add(dataurl);
            s.setCover(null);
        }
        System.out.println("AAAAAAAAAAAAAAAAAAA");
        System.out.println(subscribecomicList);
        session.setAttribute("subscribeSeriesList",subscribecomicList);
        session.setAttribute("seriesCoverList",coverList);

        return "subscription";
    }











    @RequestMapping("/makegif")
    public String asdsad(HttpServletRequest request) throws SQLException, IOException {
        Iterable<Page> a = pageRepository.findAll();
        ArrayList<String> aa = new ArrayList<String>();
        for (Page p : a){

            Blob pblob=p.getContent();
            String dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(pblob.getBytes(1,(int)pblob.length()));
            aa.add(dataurl);
        }







        URL u = new URL("https://www.baidu.com/s?wd=h");
        URLConnection conn = u.openConnection();
        conn.setDoOutput(true);
        InputStream is = conn.getInputStream();
   /*     for (int i = 0; i < is.available(); i++) {
            System.out.println("" + is.read());
        }
*/


        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }

        System.out.println(buffer);

        return "test";


    }

    @RequestMapping("setCover")
    public String setCover(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        Series s=(Series)session.getAttribute("currentseries");
        int ci=Integer.parseInt(request.getParameter("chapterindex"));
        int cn=s.getChapternumber();
        List<Comic> d = comicRepository.findBySeriesid(s.getId());
        ArrayList<Comic> chapters = new ArrayList<Comic>();
        for (int i = 0; i < ci+1; i++) {
            for (int j = 0; j < ci+1; j++) {
                if (d.get(j).getIndexs() == i + 1) {
                    chapters.add(d.get(j));
                }
            }
        }
        Page p = pageRepository.findByComicidAndIndexs(chapters.get(ci).getId(), 1).get(0);
        Blob ccover = p.getContent();
        String curl = "";
        if (ccover == null) {
            curl = "";
        } else {
            curl = "data:image/png;base64," + DatatypeConverter.printBase64Binary(ccover.getBytes(1, (int) ccover.length()));
        }
        s.setCover(ccover);
        seriesRepository.save(s);

        return "viewChapters";
    }

    @RequestMapping("/seriesaddcomment")
    public String seriesaddcomment(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String comment=request.getParameter("comment");
        Seriescomment sc=new Seriescomment();
        Series s=(Series)session.getAttribute("series");
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        System.out.println(currentTimestamp);
        sc.setContent(comment);
        sc.setDate(currentTimestamp);
        sc.setSeriesid(s.getId());
        sc.setUserid(Integer.parseInt(session.getAttribute("userid")+""));
        seriescommentRepository.save(sc);
        ArrayList<Seriescomment> scs=(ArrayList<Seriescomment>)seriescommentRepository.findBySeriesid(s.getId());
        ArrayList<User> users=new ArrayList<User>();
        ArrayList<String> urls=new ArrayList<String>();

        for(int i=0;i<scs.size();i++){
            User u = userRepository.findById(scs.get(i).getUserid()).get();
            Blob pip = u.getProfileimage();
            String url = "";
            if (pip == null) {
            }
            else {
                url = "data:image/png;base64," + DatatypeConverter.printBase64Binary(pip.getBytes(1, (int) pip.length()));
            }
            users.add(u);
            urls.add(url);
            u.setProfileimage(null);

        }
        session.setAttribute("seriescomments", scs);
        session.setAttribute("commentusers", users);
        session.setAttribute("commentimages", urls);
        return "series";
    }


    @RequestMapping("/comicaddcomment")
    public String comicaddcomment(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        String comment=request.getParameter("comment");
        Comiccomment cc=new Comiccomment();
        int cid=Integer.parseInt(session.getAttribute("commentcomicid")+"");

        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        System.out.println(currentTimestamp);
        cc.setContent(comment);
        cc.setDate(currentTimestamp);
        cc.setComicid(cid);
        cc.setUserid(Integer.parseInt(session.getAttribute("userid")+""));
        comiccommentRepository.save(cc);

        ArrayList<Comiccomment> ccs=(ArrayList<Comiccomment>)comiccommentRepository.findByComicid(cid);
        ArrayList<User> users=new ArrayList<User>();
        ArrayList<String> urls=new ArrayList<String>();

        for(int i=0;i<ccs.size();i++){
            User u = userRepository.findById(ccs.get(i).getUserid()).get();
            Blob pip = u.getProfileimage();
            String url = "";
            if (pip == null) {
            }
            else {
                url = "data:image/png;base64," + DatatypeConverter.printBase64Binary(pip.getBytes(1, (int) pip.length()));
            }
            users.add(u);
            urls.add(url);
            u.setProfileimage(null);

        }
        session.setAttribute("comiccomments", ccs);
        session.setAttribute("comiccommentusers", users);
        session.setAttribute("comiccommentimages", urls);
        return "read";
    }

    @RequestMapping("/read")
    public String pageinfo(HttpServletRequest request) throws SQLException {
        Iterable<Series> serieslist = seriesRepository.findAll();
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }

        Iterable<Page> a = pageRepository.findAll();
        Iterable<Comic> b = comicRepository.findAll();
        int seriesid = 0;
        int comicindex = 0;
        int previouschapterid = -1;
        int nextchapterid = -1;
        int comicid = Integer.parseInt(request.getParameter("id"));
        int pageindex = Integer.parseInt(request.getParameter("index"));
        session.setAttribute("commentcomicid",comicid);
        for (Comic c : b) {
            if (c.getId().equals(comicid)) {
                seriesid = c.getSeriesid();
                comicindex = c.getIndexs();
                break;
            }
        }
        for (Series s : serieslist) {
            if (s.getId().equals(seriesid)) {
                session.setAttribute("series", s);
            }
        }
        List<Page> list2 = pageRepository.findByComicid(comicid);
        List<Comic> list3 = comicRepository.findBySeriesid(seriesid);

        Boolean aim1 = false;
        Boolean aim2 = false;

        for (Comic c : list3) {
            if (aim1 && aim2) {
                break;
            }
            if (c.getIndexs().equals(comicindex - 1)) {
                previouschapterid = c.getId();
                aim1 = true;
            }
            if (c.getIndexs().equals(comicindex + 1)) {
                nextchapterid = c.getId();
                aim2 = true;
            }
        }
        session.setAttribute("chapternumber", list3.size());
        session.setAttribute("comicindex", comicindex);
        session.setAttribute("pagenumber", list2.size());
        session.setAttribute("nextchapterid", nextchapterid);
        session.setAttribute("previouschapterid", previouschapterid);
        List<Page> list = pageRepository.findByComicidAndIndexs(comicid, pageindex);
        if (list.size() == 0) {
            session.setAttribute("page", null);
            session.setAttribute("status", 2);
        } else {
            Blob page = list.get(0).getContent();
            String dataurl = "data:image/png;base64," + DatatypeConverter.printBase64Binary(page.getBytes(1, (int) page.length()));

            List<History> his = historyRepository.findBySeriesid(seriesid);
            if (his.size() != 0) {
                historyRepository.deleteById(his.get(0).getId());
            }

            History hi = new History();
            hi.setSeriesid(seriesid);
            hi.setUserid(Integer.parseInt(session.getAttribute("userid") + ""));
            hi.setComicid(comicid);
            hi.setPageindex(pageindex);
            historyRepository.save(hi);


            session.setAttribute("page", list.get(0));
            session.setAttribute("status", 1);
            session.setAttribute("pagecontent", dataurl);

            ArrayList<Comiccomment> ccs=(ArrayList<Comiccomment>)comiccommentRepository.findByComicid(comicid);
            ArrayList<User> users=new ArrayList<User>();
            ArrayList<String> urls=new ArrayList<String>();

            for(int i=0;i<ccs.size();i++){
                User u = userRepository.findById(ccs.get(i).getUserid()).get();
                Blob pip = u.getProfileimage();
                String url = "";
                if (pip == null) {

                }
                else {
                    url = "data:image/png;base64," + DatatypeConverter.printBase64Binary(pip.getBytes(1, (int) pip.length()));
                }
                users.add(u);
                urls.add(url);
                u.setProfileimage(null);
            }
            session.setAttribute("comiccomments", ccs);
            session.setAttribute("comiccommentusers", users);
            session.setAttribute("comiccommentimages", urls);
        }

        return "read";

    }



}
