/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Arne Kepp / The Open Planning Project 2008
 * @author David Vick / Boundless 2017
 *     <p>Original file IndexRestlet.java
 */
package org.geowebcache.rest.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping(path = "${gwc.context.suffix:}/rest")
public class GWCIndexController {

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public ResponseEntity<?> handleRequestInternal(HttpServletRequest request) {
        String idx =
                "<html>\n"
                        + "<meta charset=\"UTF-8\">\n"
                        + "<title>webcacheRest服务主页面</title>\n"
                        + "<link rel=\"stylesheet\" href=\"rest/web/gwc.css\" type=\"text/css\"/>\n"
                        + "</head>"
                        + "<body>\n"
                        + "<a id=\"logo\" href=\""
                        + request.getRequestURI().toString()
                        + "\">"
                        + "<img src=\""
                        + request.getRequestURI()
                        + "/web/geowebcache_logo.png\" alt=\"\" height=\"100\" width=\"353\" border=\"0\"/></a>\n"
                        + "<h3>获取所有图层:</h3>"
                        + "<ul>"
                        + "<li><h4><a href=\""
                        + request.getRequestURI()
                        + "/layers/\">所有图层</a></h4>"
                        + "让您看到配置的图层 "
                        + " 您还可以通过将图层名称附加到 URL、删除现有图层或发布新图层来查看特定图层 "
                        + " 请注意，仅当通过 geowebcache.xml 配置了 GeoWebCache 时，"
                        + " 后面的操作才有意义。您可以发布 XML 或 JSON。"
                        + "</li>\n"
                        + "</ul>\n"
                        + "<h3>获取所有切片方案:</h3>"
                        + "<ul>\n"
                        + "<li><h4><a href=\""
                        + request.getRequestURI()
                        + "/gridsets/\">所有切片方案</a></h4>可以让您看到现在保存的所有切片方案 </li>"
                        + "<li><h4>切片任务</h4>"
                        + ""
                        + "</li>\n"
                        + "</ul>"
                        + "</body></html>";
        return new ResponseEntity<>(idx, HttpStatus.OK);
    }
}
