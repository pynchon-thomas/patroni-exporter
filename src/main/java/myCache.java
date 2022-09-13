import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;


import java.util.*;

public class myCache {
    Collection<Metrics> collector;
    public void setCollector(Collection<Metrics> collector){
        this.collector = collector;
    }
    CacheLoader<String, String[][]> loader = new CacheLoader<String, String[][]>() {
        @Override
        public String[][] load(String s) {
            return getLabels(s);
        }
    };

    Cache<String, String[][]> loadingCache = CacheBuilder.newBuilder().maximumSize(2).build(loader);

    String[][] getLabels(String s) {
        String[][] m = new String[2][2];
        for (Metrics i: collector){

            if(i.name.equals(s)){
                Integer num = i.getLabels().size();
                String[] k =new String[num];
                String[] v = new String[num];

                Integer[] n = new Integer[1];n[0] =0;

                i.getLabels().entrySet().forEach((e)->{

                    k[n[0]] = e.getKey();
                    v[n[0]] = e.getValue();
                    n[0]++;

                });
                m[0]=k;m[1]=v;

            }
        }
        return m;
    }
}
