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
 * @author Marius Suta / The Open Planning Project 2008
 * @author Arne Kepp / The Open Planning Project 2009
 * @author David Vick / Boundless 2017
 *     <p>Original file
 *     <p>SeedFormRestlet.java
 */
package org.geowebcache.rest.service;

import static org.geowebcache.seed.TileBreeder.TILE_FAILURE_RETRY_COUNT_DEFAULT;
import static org.geowebcache.seed.TileBreeder.TILE_FAILURE_RETRY_WAIT_TIME_DEFAULT;
import static org.geowebcache.seed.TileBreeder.TOTAL_FAILURES_BEFORE_ABORTING_DEFAULT;
import static org.geowebcache.seed.TileBreeder.createTileRange;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.parameters.FloatParameterFilter;
import org.geowebcache.filter.parameters.ParameterFilter;
import org.geowebcache.filter.parameters.RegexParameterFilter;
import org.geowebcache.filter.parameters.StringParameterFilter;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.mime.MimeType;
import org.geowebcache.rest.exception.RestException;
import org.geowebcache.seed.GWCTask;
import org.geowebcache.seed.SeedRequest;
import org.geowebcache.seed.TileBreeder;
import org.geowebcache.storage.TileRange;
import org.geowebcache.util.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class FormService {

    @Autowired TileBreeder seeder;

    public ResponseEntity<?> handleKillAllThreadsPost(Map<String, String> form, TileLayer tl)
            throws RestException {

        final boolean allLayers = tl == null;

        String killCode = form.get("kill_all");

        final Iterator<GWCTask> tasks;
        if ("1".equals(killCode) || "running".equalsIgnoreCase(killCode)) {
            killCode = "running";
            tasks = seeder.getRunningTasks();
        } else if ("pending".equalsIgnoreCase(killCode)) {
            tasks = seeder.getPendingTasks();
        } else if ("all".equalsIgnoreCase(killCode)) {
            tasks = seeder.getRunningAndPendingTasks();
        } else {
            throw new RestException(
                    "Unknown kill_all code: '"
                            + killCode
                            + "'. One of all|running|pending is expected.",
                    HttpStatus.BAD_REQUEST);
        }

        List<GWCTask> terminatedTasks = new LinkedList<>();
        List<GWCTask> nonTerminatedTasks = new LinkedList<>();
        while (tasks.hasNext()) {
            GWCTask task = tasks.next();
            String layerName = task.getLayerName();
            if (!allLayers && !tl.getName().equals(layerName)) {
                continue;
            }
            long taskId = task.getTaskId();
            boolean terminated = seeder.terminateGWCTask(taskId);
            if (terminated) {
                terminatedTasks.add(task);
            } else {
                nonTerminatedTasks.add(task);
            }
        }
        StringBuilder doc = new StringBuilder();

        makeHeader(doc);
        doc.append("<p>终止 ").append(killCode).append(" 任务.");
        doc.append("终止的任务: <ul>");
        for (GWCTask t : terminatedTasks) {
            doc.append("<li>").append(t).append("</li>");
        }
        doc.append("</ul>已完成的任务: <ul>");
        for (GWCTask t : nonTerminatedTasks) {
            doc.append("<li>").append(t).append("</li>");
        }
        if (tl != null) {
            doc.append("</ul><p><a href=\"./" + tl.getName() + "\">返回</a></p>\n");
        }

        return new ResponseEntity<>(doc.toString(), getHeaders(), HttpStatus.OK);
    }

    public ResponseEntity<?> handleKillThreadPost(Map<String, String> form, TileLayer tl) {
        String id = form.get("thread_id");

        StringBuilder doc = new StringBuilder();

        makeHeader(doc);

        if (seeder.terminateGWCTask(Long.parseLong(id))) {
            doc.append("<ul><li>请求终止任务 " + id + ".</li></ul>");
        } else {
            doc.append("<ul><li>抱歉，选择的任务 " + id + " 尚未启动，或者是无法中断的截断任务。.</li></ul>");
        }

        if (tl != null) {
            doc.append("<p><a href=\"./" + tl.getName() + "\">返回</a></p>\n");
        }

        return new ResponseEntity<Object>(doc.toString(), getHeaders(), HttpStatus.OK);
    }

    public ResponseEntity<?> handleFormPost(String layer, Map<String, String> params)
            throws RestException, GeoWebCacheException {
        final TileLayer tl;
        {
            String layerName = layer;
            if (layer != null) {
                try {
                    tl = seeder.findTileLayer(layerName);
                } catch (GeoWebCacheException e) {
                    throw new RestException(e.getMessage(), HttpStatus.BAD_REQUEST);
                }
            } else {
                tl = null;
            }
        }

        if (params.containsKey("list")) {
            if (tl == null) {
                throw new RestException("没有指定图层", HttpStatus.BAD_REQUEST);
            }
            boolean listAllTasks = "all".equals(params.get("list"));
            return handleDoGet(tl, listAllTasks);
        } else if (params.containsKey("kill_thread")) {
            return handleKillThreadPost(params, tl);
        } else if (params.containsKey("kill_all")) {
            return handleKillAllThreadsPost(params, tl);
        } else if (params.get("minX") != null) {
            if (tl == null) {
                throw new RestException("没有指定图层", HttpStatus.BAD_REQUEST);
            }
            return handleDoSeedPost(params, tl);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> handleDoSeedPost(Map<String, String> form, TileLayer tl)
            throws RestException, GeoWebCacheException {
        String gridSetId = form.get("gridSetId");

        BoundingBox bounds = null;

        if (form.get("minX") != null && !form.get("minX").equalsIgnoreCase("")) {
            bounds =
                    new BoundingBox(
                            parseDouble(form, "minX"),
                            parseDouble(form, "minY"),
                            parseDouble(form, "maxX"),
                            parseDouble(form, "maxY"));
        } else {
            // Use default values for tile layer
            GridSubset subset = tl.getGridSubset(gridSetId);
            bounds = subset.getOriginalExtent();
        }

        int threadCount = Integer.parseInt(form.get("threadCount"));
        int zoomStart = Integer.parseInt(form.get("zoomStart"));
        int zoomStop = Integer.parseInt(form.get("zoomStop"));
        String format = form.get("tileFormat");
        Map<String, String> fullParameters;
        {
            Map<String, String> parameters = new HashMap<>();
            Set<String> paramNames = form.keySet();
            String prefix = "parameter_";
            for (String name : paramNames) {
                if (name.startsWith(prefix)) {
                    String paramName = name.substring(prefix.length());
                    String value = form.get(name);
                    parameters.put(paramName, value);
                }
            }
            fullParameters = tl.getModifiableParameters(parameters, "UTF-8");
        }

        GWCTask.TYPE type = GWCTask.TYPE.valueOf(form.get("type").toUpperCase());

        final String layerName = tl.getName();
        SeedRequest sr =
                new SeedRequest(
                        layerName,
                        bounds,
                        gridSetId,
                        threadCount,
                        zoomStart,
                        zoomStop,
                        format,
                        type,
                        fullParameters);

        int tileFailureRetryCount = (int) getOptionalLongParam(form, "tileFailureRetryCount", 0);
        long tileFailureRetryWaitTime = getOptionalLongParam(form, "tileFailureRetryWaitTime", 0);
        long totalFailuresBeforeAborting =
                getOptionalLongParam(form, "totalFailuresBeforeAborting", 0);
        TileRange tr = createTileRange(sr, tl);

        GWCTask[] tasks;
        try {
            tasks =
                    seeder.createTasks(
                            tr,
                            tl,
                            sr.getType(),
                            sr.getThreadCount(),
                            sr.getFilterUpdate(),
                            tileFailureRetryCount,
                            tileFailureRetryWaitTime,
                            totalFailuresBeforeAborting);
        } catch (GeoWebCacheException e) {
            throw new RestException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        seeder.dispatchTasks(tasks);

        // Give the thread executor a chance to run
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ok, no worries
            Thread.currentThread().interrupt();
        }

        return new ResponseEntity<Object>(this.makeResponsePage(tl), getHeaders(), HttpStatus.OK);
    }

    private long getOptionalLongParam(Map<String, String> form, String key, long defaultValue) {
        String value = form.get(key);
        if (value == null) return defaultValue;
        return Long.parseLong(value);
    }

    private static double parseDouble(Map<String, String> form, String key) throws RestException {
        String value = form.get(key);
        if (value == null || value.length() == 0)
            throw new RestException("缺少参数 " + key, HttpStatus.BAD_REQUEST);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new RestException("参数 " + key + " 不是double类型", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> handleGet(HttpServletRequest request, String layer) {
        TileLayer tl;
        try {
            tl = seeder.findTileLayer(layer);
        } catch (GeoWebCacheException e) {
            throw new RestException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return handleDoGet(tl, false);
    }

    public void setTileBreeder(TileBreeder seeder) {
        this.seeder = seeder;
    }

    private ResponseEntity<?> handleDoGet(TileLayer tl, boolean listAllTasks) {
        return new ResponseEntity<>(makeFormPage(tl, listAllTasks), getHeaders(), HttpStatus.OK);
    }

    protected String makeFormPage(TileLayer tl, boolean listAllTasks) {

        StringBuilder doc = new StringBuilder();

        makeHeader(doc);

        makeTaskList(doc, tl, listAllTasks);

        makeWarningsAndHints(doc, tl);

        makeFormHeader(doc, tl);

        makeThreadCountPullDown(doc);

        makeTypePullDown(doc);

        makeGridSetPulldown(doc, tl);

        makeFormatPullDown(doc, tl);

        makeZoomStartPullDown(doc, tl);

        makeZoomStopPullDown(doc, tl);

        makeModifiableParameters(doc, tl);

        makeBboxFields(doc);

        makeFailureHandlingPolicies(doc);

        makeSubmit(doc);

        makeFormFooter(doc);

        makeFooter(doc);

        return doc.toString();
    }

    private void makeModifiableParameters(StringBuilder doc, TileLayer tl) {
        List<ParameterFilter> parameterFilters = tl.getParameterFilters();
        if (parameterFilters == null || parameterFilters.isEmpty()) {
            return;
        }
        doc.append("<tr><td>可修改参数:</td><td>\n");
        doc.append("<table>");
        for (ParameterFilter pf : parameterFilters) {
            Assert.notNull(pf, "参数过滤器必须非空");
            String key = pf.getKey();
            String defaultValue = pf.getDefaultValue();
            List<String> legalValues = pf.getLegalValues();
            doc.append("<tr><td>").append(key.toUpperCase()).append(": ").append("</td><td>");
            String parameterId = "parameter_" + key;
            if (pf instanceof StringParameterFilter) {
                Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                makePullDown(doc, parameterId, keysValues, defaultValue);
            } else if (pf instanceof RegexParameterFilter) {
                makeTextInput(doc, parameterId, 25);
            } else if (pf instanceof FloatParameterFilter) {
                FloatParameterFilter floatFilter = (FloatParameterFilter) pf;
                if (floatFilter.getValues().isEmpty()) {
                    // accepts any value
                    makeTextInput(doc, parameterId, 25);
                } else {
                    Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                    makePullDown(doc, parameterId, keysValues, defaultValue);
                }
            } else if ("org.geowebcache.filter.parameters.NaiveWMSDimensionFilter"
                    .equals(pf.getClass().getName())) {
                makeTextInput(doc, parameterId, 25);
            } else {
                // Unknown filter type
                if (legalValues == null) {
                    // Doesn't have a defined set of values, just provide a text field
                    makeTextInput(doc, parameterId, 25);
                } else {
                    // Does have a defined set of values, so provide a drop down
                    Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                    makePullDown(doc, parameterId, keysValues, defaultValue);
                }
            }
            doc.append("</td></tr>");
        }
        doc.append("</table>");
        doc.append("</td></tr>\n");
    }

    private Map<String, String> makeParametersMap(String defaultValue, List<String> legalValues) {
        Map<String, String> map = new TreeMap<>();
        for (String s : legalValues) {
            map.put(s, s);
        }
        map.put(defaultValue, defaultValue);
        return map;
    }

    private String makeResponsePage(TileLayer tl) {

        StringBuilder doc = new StringBuilder();

        makeHeader(doc);

        doc.append("<h3>Task submitted</h3>\n");

        doc.append("<p>您可以在下面找到当前正在执行的任务列表，请谨慎对待这些数字");
        doc.append(" 直到任务有机会运行几分钟。 ");

        makeTaskList(doc, tl, false);

        makeFooter(doc);

        return doc.toString();
    }

    private void makeTypePullDown(StringBuilder doc) {
        doc.append("<tr><td>操作类型:</td><td>\n");
        Map<String, String> keysValues = new TreeMap<>();

        keysValues.put("Truncate - remove tiles", "truncate");
        keysValues.put("Seed - generate missing tiles", "seed");
        keysValues.put("Reseed - regenerate all tiles", "reseed");

        makePullDown(doc, "type", keysValues, "Seed - generate missing tiles");
        doc.append("</td></tr>\n");
    }

    private void makeThreadCountPullDown(StringBuilder doc) {
        doc.append("<tr><td>要使用的任务数:</td><td>\n");
        Map<String, String> keysValues = new LinkedHashMap<>();

        for (int i = 1; i < 129; i++) {
            if (i < 10) {
                keysValues.put("0" + Integer.toString(i), "0" + Integer.toString(i));
            } else {
                keysValues.put(Integer.toString(i), Integer.toString(i));
            }
        }
        makePullDown(doc, "threadCount", keysValues, Integer.toString(2));
        doc.append("</td></tr>\n");
    }

    private void makeBboxFields(StringBuilder doc) {
        doc.append("<tr><td valign=\"top\">边界框:</td><td>\n");
        makeTextInput(doc, "minX", 6);
        makeTextInput(doc, "minY", 6);
        makeTextInput(doc, "maxX", 6);
        makeTextInput(doc, "maxY", 6);
        doc.append("</br><span style=\"font-size:80%\">这些是可选的，近似值很好.</span>");
        doc.append("</td></tr>\n");
    }

    private void makeFailureHandlingPolicies(StringBuilder doc) {
        doc.append("<tr><td valign=\"top\">瓦片失败重试:</td><td>");
        makeTextInput(doc, "tile 失败重试次数", 6, String.valueOf(TILE_FAILURE_RETRY_COUNT_DEFAULT));
        doc.append("</br><span style=\"font-size:80%\">设置为 -1 以禁用重试并在第一次失败时停止切片线程.</span>");
        doc.append("</td></tr>");
        doc.append("<tr><td valign=\"top\">重试前暂停 (ms):</td><td>");
        makeTextInput(
                doc,
                "tileFailureRetryWaitTime",
                6,
                String.valueOf(TILE_FAILURE_RETRY_WAIT_TIME_DEFAULT));
        doc.append("</td></tr>");
        doc.append("<tr><td valign=\"top\">中止前的总失败次数:</td><td>");
        makeTextInput(
                doc,
                "totalFailuresBeforeAborting",
                6,
                String.valueOf(TOTAL_FAILURES_BEFORE_ABORTING_DEFAULT));
        doc.append("</td></tr>");
    }

    private void makeBboxHints(StringBuilder doc, TileLayer tl) {

        for (String gridSetId : tl.getGridSubsets()) {
            GridSubset subset = tl.getGridSubset(gridSetId);
            doc.append(
                    "<li>"
                            + gridSetId
                            + ":   "
                            + subset.getOriginalExtent().toString()
                            + "</li>\n");
        }
    }

    private void makeTextInput(StringBuilder doc, String id, int size) {
        makeTextInput(doc, id, size, "");
    }

    private void makeTextInput(StringBuilder doc, String id, int size, String defaultValue) {
        doc.append(
                "<input name=\""
                        + id
                        + "\" type=\"text\" size=\""
                        + size
                        + "\" value=\""
                        + defaultValue
                        + "\"/>");
    }

    private void makeSubmit(StringBuilder doc) {
        doc.append("<tr><td></td><td><input type=\"submit\" value=\"Submit\"></td></tr>\n");
    }

    private void makeZoomStopPullDown(StringBuilder doc, TileLayer tl) {
        doc.append("<tr><td>切片停止级别:</td><td>\n");
        makeZoomPullDown(doc, false, tl);
        doc.append("</td></tr>\n");
    }

    private void makeZoomStartPullDown(StringBuilder doc, TileLayer tl) {
        doc.append("<tr><td>切片开始级别:</td><td>\n");
        makeZoomPullDown(doc, true, tl);
        doc.append("</td></tr>\n");
    }

    private void makeZoomPullDown(StringBuilder doc, boolean isStart, TileLayer tl) {
        Map<String, String> keysValues = new TreeMap<>();

        int minStart = Integer.MAX_VALUE;
        int maxStop = Integer.MIN_VALUE;

        for (String gridSetId : tl.getGridSubsets()) {
            GridSubset subset = tl.getGridSubset(gridSetId);

            int start = subset.getZoomStart();
            int stop = subset.getZoomStop();

            if (start < minStart) {
                minStart = start;
            }
            if (stop > maxStop) {
                maxStop = stop;
            }
        }

        for (int i = minStart; i <= maxStop; i++) {
            if (i < 10) {
                keysValues.put("0" + Integer.toString(i), "0" + Integer.toString(i));
            } else {
                keysValues.put(Integer.toString(i), Integer.toString(i));
            }
        }

        if (isStart) {
            if (minStart < 10) {
                makePullDown(doc, "zoomStart", keysValues, "0" + Integer.toString(minStart));
            } else {
                makePullDown(doc, "zoomStart", keysValues, Integer.toString(minStart));
            }

        } else {
            int midStop = (minStart + maxStop) / 2;
            if (midStop < 10) {
                makePullDown(doc, "zoomStop", keysValues, "0" + Integer.toString(midStop));
            } else {
                makePullDown(doc, "zoomStop", keysValues, Integer.toString(midStop));
            }
        }
    }

    private void makeFormatPullDown(StringBuilder doc, TileLayer tl) {
        doc.append("<tr><td>格式化:</td><td>\n");
        Map<String, String> keysValues = new TreeMap<>();

        Iterator<MimeType> iter = tl.getMimeTypes().iterator();

        while (iter.hasNext()) {
            MimeType mime = iter.next();
            keysValues.put(mime.getFormat(), mime.getFormat());
        }

        makePullDown(doc, "tileFormat", keysValues, ImageMime.png.getFormat());
        doc.append("</td></tr>\n");
    }

    private void makeGridSetPulldown(StringBuilder doc, TileLayer tl) {
        doc.append("<tr><td>切片方案:</td><td>\n");
        Map<String, String> keysValues = new TreeMap<>();

        String firstGridSetId = null;
        for (String gridSetId : tl.getGridSubsets()) {
            if (firstGridSetId == null) {
                firstGridSetId = gridSetId;
            }
            keysValues.put(gridSetId, gridSetId);
        }

        makePullDown(doc, "gridSetId", keysValues, firstGridSetId);
        doc.append("</td></tr>\n");
    }

    private void makePullDown(
            StringBuilder doc, String id, Map<String, String> keysValues, String defaultKey) {
        doc.append("<select name=\"" + id + "\">\n");

        Iterator<Map.Entry<String, String>> iter = keysValues.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            if (entry.getKey().equals(defaultKey)) {
                doc.append(
                        "<option value=\""
                                + entry.getValue()
                                + "\" selected=\"selected\">"
                                + entry.getKey()
                                + "</option>\n");
            } else {
                doc.append(
                        "<option value=\""
                                + entry.getValue()
                                + "\">"
                                + entry.getKey()
                                + "</option>\n");
            }
        }

        doc.append("</select>\n");
    }

    private void makeFormHeader(StringBuilder doc, TileLayer tl) {
        doc.append("<h4>创建新任务:</h4>\n");
        doc.append("<form id=\"seed\" action=\"./" + tl.getName() + "\" method=\"post\">\n");
        doc.append("<table border=\"0\" cellspacing=\"10\">\n");
    }

    private void makeFormFooter(StringBuilder doc) {
        doc.append("</table>\n");
        doc.append("</form>\n");
    }

    private void makeHeader(StringBuilder doc) {
        doc.append(
                "<html>\n"
                        + ServletUtils.gwcHtmlHeader("../../", "GWC Seed Form")
                        + "<body>\n"
                        + ServletUtils.gwcHtmlLogoLink("../../"));
    }

    private void makeWarningsAndHints(StringBuilder doc, TileLayer tl) {
        doc.append(
                "<h4>请注意:</h4><ul>\n"
                        + "<li>这个简约的界面不检查正确性.</li>\n"
                        + "<li>通常不建议切片超过缩放级别 20.</li>\n"
                        + "<li>对 KML 切片也会清理所有 KMZ 信息.</li>\n"
                        + "<li>请检查容器的日志以查找错误消息和进度指示器。</li>\n"
                        + "</ul>\n");

        doc.append("这是最大边界，如果您不指定边界，将使用这些.\n");
        doc.append("<ul>\n");
        makeBboxHints(doc, tl);
        doc.append("</ul>\n");
    }

    private void makeTaskList(StringBuilder doc, TileLayer tl, boolean listAll) {

        doc.append(makeKillallThreadsForm(tl, listAll));

        doc.append("<h4>当前正在执行的任务列表:</h4>\n");

        Iterator<GWCTask> iter = seeder.getRunningAndPendingTasks();

        boolean tasks = false;
        if (!iter.hasNext()) {
            doc.append("<ul><li><i>空</i></li></ul>\n");
        } else {
            doc.append("<table border=\"0\">");
            doc.append(
                    "<tr style=\"font-weight: bold;\"><td style=\"padding-right:20px;\">Id</td><td style=\"padding-right:20px;\">Layer</td><td style=\"padding-right:20px;\">Status</td><td style=\"padding-right:20px;\">Type</td><td>估计的 # 切片数量</td>"
                            + "<td style=\"padding-right:20px;\">切片完成</td><td style=\"padding-right:20px;\">已进行时间</td><td>剩余时间</td><td>Tasks</td><td>&nbsp;</td>");
            doc.append("</tr>");
            tasks = true;
        }

        int row = 0;

        final String layerName = tl.getName();
        while (iter.hasNext()) {
            GWCTask task = iter.next();
            if (!listAll && !layerName.equals(task.getLayerName())) {
                continue;
            }
            final long spent = task.getTimeSpent();
            final long remining = task.getTimeRemaining();
            final long tilesDone = task.getTilesDone();
            final long tilesTotal = task.getTilesTotal();

            NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
            nf.setGroupingUsed(true);
            final String tilesTotalStr;
            if (tilesTotal < 0) {
                tilesTotalStr = "Too many to count";
            } else {
                tilesTotalStr = nf.format(tilesTotal);
            }
            final String tilesDoneStr = nf.format(task.getTilesDone());
            final GWCTask.STATE state = task.getState();

            final String status =
                    GWCTask.STATE.UNSET.equals(state) || GWCTask.STATE.READY.equals(state)
                            ? "PENDING"
                            : state.toString();

            String timeSpent = toTimeString(spent, tilesDone, tilesTotal);
            String timeRemaining = toTimeString(remining, tilesDone, tilesTotal);

            String bgColor = ++row % 2 == 0 ? "#FFFFFF" : "#DDDDDD";
            doc.append("<tr style=\"background-color:" + bgColor + ";\">");
            doc.append("<td style=\"text-align:right\">").append(task.getTaskId()).append("</td>");
            doc.append("<td>");
            if (!layerName.equals(task.getLayerName())) {
                doc.append("<a href=\"./").append(task.getLayerName()).append("\">");
            }
            doc.append(task.getLayerName());
            if (!layerName.equals(task.getLayerName())) {
                doc.append("</a>");
            }
            doc.append("</td>");
            doc.append("<td>").append(status).append("</td>");
            doc.append("<td>").append(task.getType()).append("</td>");
            doc.append("<td>").append(tilesTotalStr).append("</td>");
            doc.append("<td>").append(tilesDoneStr).append("</td>");
            doc.append("<td>").append(timeSpent).append("</td>");
            doc.append("<td>").append(timeRemaining).append("</td>");
            doc.append("<td>(Task ")
                    .append(task.getThreadOffset() + 1)
                    .append(" of ")
                    .append(task.getThreadCount())
                    .append(") </td>");
            doc.append("<td>").append(makeThreadKillForm(task.getTaskId(), tl)).append("</td>");
            doc.append("<tr>");
        }

        if (tasks) {
            doc.append("</table>");
        }
        doc.append("<p><a href=\"./" + layerName + "\">刷新列表</a></p>\n");
    }

    private String toTimeString(long timeSeconds, final long tilesDone, final long tilesTotal) {
        String timeString;
        if (tilesDone < 50) {
            timeString = " Estimating...";
        } else {
            final int MINUTE_SECONDS = 60;
            final int HOUR_SECONDS = MINUTE_SECONDS * 60;
            final int DAY_SECONDS = HOUR_SECONDS * 24;

            if (timeSeconds == -2 && tilesDone < tilesTotal) {
                timeString = " A decade or three.";
            } else {
                if (timeSeconds > DAY_SECONDS) {
                    long days = timeSeconds / DAY_SECONDS;
                    long hours = (timeSeconds % DAY_SECONDS) / HOUR_SECONDS;
                    timeString = days + " day" + (days > 1 ? "s " : " ");
                    timeString += hours == 0 ? "" : (hours + " h");
                } else if (timeSeconds > HOUR_SECONDS) {
                    long hours = timeSeconds / HOUR_SECONDS;
                    long minutes = (timeSeconds % HOUR_SECONDS) / MINUTE_SECONDS;
                    timeString = hours + " hour" + (hours > 1 ? "s " : " ");
                    timeString += minutes == 0 ? "" : (minutes + " m");
                } else if (timeSeconds > MINUTE_SECONDS) {
                    long minutes = timeSeconds / MINUTE_SECONDS;
                    long seconds = timeSeconds % MINUTE_SECONDS;
                    timeString = minutes + " minute" + (minutes > 1 ? "s " : " ");
                    timeString += seconds == 0 ? "" : seconds + " s";
                } else {
                    timeString = timeSeconds + " second" + (timeSeconds == 1 ? "" : "s");
                }
            }
        }
        return timeString;
    }

    private String makeThreadKillForm(Long key, TileLayer tl) {
        String ret =
                "<form form id=\"kill\" action=\"./"
                        + tl.getName()
                        + "\" method=\"post\">"
                        + "<input type=\"hidden\" name=\"kill_thread\"  value=\"1\" />"
                        + "<input type=\"hidden\" name=\"thread_id\"  value=\""
                        + key
                        + "\" />"
                        + "<span><input style=\"padding: 0; margin-bottom: -12px; border: 1;\"type=\"submit\" value=\"Kill Task\"></span>"
                        + "</form>";

        return ret;
    }

    private String makeKillallThreadsForm(TileLayer tl, boolean listAll) {
        StringBuilder doc = new StringBuilder();

        final String layerName = tl.getName();
        int otherLayersTaskCount = 0;
        if (!listAll) {
            Iterator<GWCTask> tasks = seeder.getRunningAndPendingTasks();
            while (tasks.hasNext()) {
                if (!layerName.equals(tasks.next().getLayerName())) {
                    otherLayersTaskCount++;
                }
            }
        }

        doc.append("<table><tr><td>");
        doc.append("<form form id=\"list\" action=\"./")
                .append(layerName)
                .append("\" method=\"post\">\n");
        doc.append("列表 ");
        doc.append("<select name=\"list\" onchange=\"this.form.submit();\">\n");
        doc.append("<option value=\"layer\"")
                .append(listAll ? "" : " selected")
                .append(">this Layer tasks</option>\n");
        doc.append("<option value=\"all\"")
                .append(listAll ? " selected" : "")
                .append(">all Layers tasks</option>\n");
        doc.append("</select>\n");
        if (!listAll) {
            doc.append(" (这里有 ");
            if (otherLayersTaskCount > 0) {
                doc.append(otherLayersTaskCount);
            } else {
                doc.append("0");
            }
            doc.append(" 个其他图层的切片任务)");
        }
        doc.append("</form>\n");

        doc.append("</td></tr><tr><td>");

        doc.append("<form form id=\"kill\" action=\"./")
                .append(layerName)
                .append("\" method=\"post\">\n");
        doc.append("<span>停止 \n");
        doc.append("<select name=\"kill_all\">\n");
        doc.append("<option value=\"all\">all</option>\n");
        doc.append("<option value=\"running\">running</option>\n");
        doc.append("<option value=\"pending\">pending</option>\n");
        doc.append("</select>\n");
        doc.append(" 图层任务'").append(layerName).append("'.");
        doc.append("<input type=\"submit\" value=\" Submit\">");
        doc.append("</span>\n");
        doc.append("</form>\n");

        doc.append("</td></tr></table>");
        return doc.toString();
    }

    private void makeFooter(StringBuilder doc) {
        doc.append("</body></html>\n");
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> utf8 = Collections.singletonMap("charset", "utf-8");
        headers.setContentType(new MediaType(MediaType.TEXT_HTML, utf8));
        return headers;
    }
}
