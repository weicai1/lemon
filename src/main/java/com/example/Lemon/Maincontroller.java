package com.example.Lemon;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.rowset.serial.SerialBlob;
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
                if(u.getIssuperuser()){
                    return "superuser";
                }
                session.setAttribute("userid",u.getId());
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

                                ////

        Iterable<Series> alls = seriesRepository.findAll();

        int limit = 6;
        ArrayList<Series> sorts = new ArrayList<>();
        for (Series s : alls) {
            sorts.add(s);
        }
        for (int i = 0;i<sorts.size()-1;i++){
            for(int j = i+1;j<sorts.size();j++){
                if(sorts.get(i).getSubnumber()<sorts.get(i).getSubnumber()){
                    Series temp = sorts.get(i);
                    sorts.set(i,sorts.get(j));
                    sorts.set(j,temp);
                }
            }
        }
        ArrayList<String> al = new ArrayList<String>();
        for(Series se:sorts){
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
        }
        session.setAttribute("series",sorts);
        session.setAttribute("covers",al);

        return "home";
    }

    @RequestMapping("/series")
    public String seriesinfo(HttpServletRequest request) throws SQLException {
        Iterable<Series> a = seriesRepository.findAll();

        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        int seriesid = Integer.parseInt(request.getParameter("id"));

        for (Series s : a) {
            if (s.getId().equals(seriesid)) {
                session.setAttribute("state", 1);
                session.setAttribute("series",s);
                Blob cover = s.getCover();
                String dataurl="";
                if(cover==null){
                    dataurl="";
                }
                else{
                    dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(cover.getBytes(1,(int)cover.length()));
                }
                session.setAttribute("seriescover",dataurl);
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
    public String pageinfo(HttpServletRequest request) throws SQLException {
        Iterable<Series> serieslist = seriesRepository.findAll();
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
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
            Blob page=list.get(0).getContent();
            String dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(page.getBytes(1,(int)page.length()));
            session.setAttribute("page",list.get(0));
            session.setAttribute("status",1);
            session.setAttribute("pagecontent",dataurl);
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
                seriesRepository.save(temp);
            }
            pageRepository.save(p);
        }

            return "drawComic";
    }

    @GetMapping("/editInformation")
    public String editInformation(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();
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


        session.setAttribute("profileimagepath",dataurl);
        return "editInformation";
    }

    @PostMapping("/editInformation")
    public String editInformation2(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException, SQLException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
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
        User u = userRepository.findById(Integer.parseInt(""+session.getAttribute("userid"))).get();
        //u.setProfileimage("profileimage/"+uuid+suffix);
        Blob blob = new SerialBlob(file.getBytes());
        u.setProfileimage(blob);
        userRepository.save(u);
        //session.setAttribute("profileimagepath","profileimage/"+uuid+suffix);


        String dataurl = "data:image/png;base64,"+DatatypeConverter.printBase64Binary(file.getBytes());
        session.setAttribute("profileimagepath",dataurl);
        //StringBuilder sb = new StringBuilder();
        //sb.append("data:image/png;base64,");
        //sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(file.getBytes(), false)));
        //contourChart = sb.toString();
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
    @RequestMapping("/message")
    public String message(HttpServletRequest request) throws IOException {
        String m=request.getParameter("data");
        if(m!=null){
/*
            URL url = new URL("https://www."+h.substring(8));

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream("image.jpg");
            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
*/

            byte[] imagedata = DatatypeConverter.parseBase64Binary(m.substring(m.indexOf(",") + 1));
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
            String pname=UUID.randomUUID().toString().replaceAll("-","")+".png";
            ImageIO.write(bufferedImage, "png", new File(pname));
            //Image im = ImageIO.read(url);

            //BufferedImage bi = new BufferedImage
            //        (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
            //Graphics bg = bi.getGraphics();
            //bg.drawImage(im, 0, 0, null);
            //bg.dispose();
            //File outputfile = new File("saved.png");
            //ImageIO.write(bi, "png", outputfile);

            /*while (h.hasMoreElements()) {
                String name =(String) h.nextElement();
                String value = request.getParameter(name);
                System.out.println(name);
                System.out.println(value);
            }*/
        }

        return "message";
    }
    @GetMapping("/personal")
    public String personal() {
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

    @RequestMapping("/toSubscribe")
    public String toSubscribe(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") == null) {
            return "redirect:/login";
        }
        int subscriber = (Integer)session.getAttribute("userid");
        int subscribedSeries = Integer.parseInt(request.getParameter("seriesid")+"");

        List<Subscribedcomic> scl = subscribedcomicRepository.findBySeriesidAndUserid(subscribedSeries,subscriber);
        if(scl.size()!=0){
            List<Subscribedcomic> sc = subscribedcomicRepository.findBySeriesidAndUserid(subscribedSeries,subscriber);
            subscribedcomicRepository.deleteById(sc.get(0).getId());
            Series seri=seriesRepository.findById(subscribedSeries).get();
            seri.setSubnumber(seri.getSubnumber()-1);
            ArrayList<Subscribedcomic> sbl = (ArrayList<Subscribedcomic>)subscribedcomicRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
            ArrayList<Integer> subscribeComicList=new ArrayList<Integer>();
            for(int i=0;i<sbl.size();i++){
                subscribeComicList.add(sbl.get(i).getSeriesid());
            }
            session.setAttribute("subscribeComicList",subscribeComicList);

            return "series";

        }
        Subscribedcomic sc = new Subscribedcomic();
        sc.setSeriesid(subscribedSeries);
        sc.setUserid(subscriber);
        subscribedcomicRepository.save(sc);
        ArrayList<Subscribedcomic> sbl = (ArrayList<Subscribedcomic>)subscribedcomicRepository.findByUserid(Integer.parseInt(""+session.getAttribute("userid")));
        ArrayList<Integer> subscribeComicList=new ArrayList<Integer>();
        for(int i=0;i<sbl.size();i++){
            subscribeComicList.add(sbl.get(i).getSeriesid());
        }
        session.setAttribute("subscribeComicList",subscribeComicList);
        Series seri=seriesRepository.findById(subscribedSeries).get();
        seri.setSubnumber(seri.getSubnumber()+1);
        return "series";
    }


}
