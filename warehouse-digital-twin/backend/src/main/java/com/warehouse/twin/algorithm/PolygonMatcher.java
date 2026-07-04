package com.warehouse.twin.algorithm;
import com.fasterxml.jackson.core.type.TypeReference; import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.stereotype.Component; import java.util.*;
@Component
public class PolygonMatcher {
    private final ObjectMapper json; public PolygonMatcher(ObjectMapper json){this.json=json;}
    public boolean matches(List<Double> bbox,String polygonJson){
        if(bbox==null||bbox.size()!=4||polygonJson==null)return false;
        try{List<List<Double>> p=json.readValue(polygonJson,new TypeReference<>(){});double cx=(bbox.get(0)+bbox.get(2))/2,cy=(bbox.get(1)+bbox.get(3))/2;return pointInPolygon(cx,cy,p)||overlapRatio(bbox,p)>=0.35;}catch(Exception e){return false;}
    }
    public boolean near(List<Double> bbox,String polygonJson,double margin){
        if(bbox==null||bbox.size()!=4||polygonJson==null)return false;
        try{List<List<Double>> p=json.readValue(polygonJson,new TypeReference<>(){});double cx=(bbox.get(0)+bbox.get(2))/2,cy=(bbox.get(1)+bbox.get(3))/2;
            double minX=p.stream().mapToDouble(x->x.get(0)).min().orElse(0),maxX=p.stream().mapToDouble(x->x.get(0)).max().orElse(0);
            double minY=p.stream().mapToDouble(x->x.get(1)).min().orElse(0),maxY=p.stream().mapToDouble(x->x.get(1)).max().orElse(0);
            double dx=Math.max(Math.max(minX-cx,0),cx-maxX),dy=Math.max(Math.max(minY-cy,0),cy-maxY);return Math.hypot(dx,dy)<=margin;
        }catch(Exception e){return false;}
    }
    public boolean pointInPolygon(double x,double y,List<List<Double>> p){boolean inside=false;for(int i=0,j=p.size()-1;i<p.size();j=i++){
        double xi=p.get(i).get(0),yi=p.get(i).get(1),xj=p.get(j).get(0),yj=p.get(j).get(1);
        if(((yi>y)!=(yj>y))&&(x<(xj-xi)*(y-yi)/(yj-yi)+xi))inside=!inside;
    }return inside;}
    public double overlapRatio(List<Double> b,List<List<Double>> p){
        double minX=p.stream().mapToDouble(x->x.get(0)).min().orElse(0),maxX=p.stream().mapToDouble(x->x.get(0)).max().orElse(0);
        double minY=p.stream().mapToDouble(x->x.get(1)).min().orElse(0),maxY=p.stream().mapToDouble(x->x.get(1)).max().orElse(0);
        double ix=Math.max(0,Math.min(b.get(2),maxX)-Math.max(b.get(0),minX)),iy=Math.max(0,Math.min(b.get(3),maxY)-Math.max(b.get(1),minY));
        double area=Math.max(1,(b.get(2)-b.get(0))*(b.get(3)-b.get(1)));return ix*iy/area;
    }
}
