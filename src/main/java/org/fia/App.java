
package org.fia;

import static spark.Spark.*;






public class App {
    public static myCache myCache = new myCache();


    public static void main(String[] args)  {

       get("/metrics", (req, res) -> {
            res.type("text/plain");

                    PatrCollect collect = new PatrCollect(args[0]);

                    collect.getMembers();
                    collect.checkNode();
                    collect.checkLeader();
                   String r= collect.expose();

                   return r;


                }
        );
    }





    }
