(ns static.test.core
  (:use [static.core] :reload)
  (:use [static.markdown] :reload)
  (:use [static.test.dummy-fs] :reload)
  (:use [clojure.test])
  (:use [clojure.contrib.io :only [delete-file-recursively]])
  (:import (java.io File)))

(defn dummy-fs-fixture [f]
  (create-dummy-fs)
  (create)
  (f)
  (delete-file-recursively (File. "resources/") true)
  (delete-file-recursively (File. "html/") true)
  (delete-file-recursively (File. "config.clj") true))

(use-fixtures :once dummy-fs-fixture)

(deftest test-markdown
  (let [[metadata content] (read-markdown "resources/site/dummy.markdown")] 
    (is (= "unit test"  (:tags metadata)))
    (is (= "some dummy desc" (:description metadata)))
    (is (= "dummy content" (:title metadata)))
    (is (= "Some dummy file for unit testing."
	   (re-find #"Some dummy file for unit testing." content)))))

(deftest test-rss-feed
  (let [rss (File. "html/rss-feed")
	content (slurp rss)] 
    (is (= true (.exists rss)))
    (is (= "<title>Dummy Site</title>"
    	   (re-find #"<title>Dummy Site</title>" content)))
    (is (= "<link>http://www.dummy.com</link>"
    	   (re-find #"<link>http://www.dummy.com</link>" content)))
    (is (= "<title>dummy future post 1</title>"
    	   (re-find #"<title>dummy future post 1</title>" content)))
    (is (= "http://www.dummy.com/2050/04/04/dummy-future-post-4/"
	   (re-find #"http://www.dummy.com/2050/04/04/dummy-future-post-4/" 
		    content)))))

(deftest test-rss-feed
  (let [tags (File. "html/tags/index.html")
	content (slurp tags)] 
    (is (= 4 (count ((tag-map) "same"))))
    (is (= true (.exists tags)))
    (is (= "<a name=\"e4e8\">e4e8</a>" 
	   (re-find #"<a name=\"e4e8\">e4e8</a>" content)))
    (is (= "<a href=\"/2050/01/01/dummy-future-post-1/\">"
    	   (re-find #"<a href=\"/2050/01/01/dummy-future-post-1/\">" 
		    content)))))

(deftest test-process-site
  (let [html (File. "html/dummy.html")
	static (File. "html/dummy.static")] 
    (is (= true (.exists html)))
    (is (= true (.exists static)))
    (is (= "Some dummy file for unit testing."
	   (re-find #"Some dummy file for unit testing." (slurp html))))
    (is (= "Hello, World!!" (re-find #"Hello, World!!" (slurp static))))))
