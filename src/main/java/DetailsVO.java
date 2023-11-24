import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.io.StringReader;
import java.util.*;

public class DetailsVO {

    static Logger LOG = LoggerFactory.getLogger(DetailsVO.class);

    private Map _searchableGroupToKeyMap;
    private Map _nonsearchableGroupToKeyMap;

    public DetailsVO() {
        _searchableGroupToKeyMap = new HashMap();
        _nonsearchableGroupToKeyMap = new HashMap();
    }

    public void addSearchableField(GroupVO gvo, String key, Object value) {
        Map removalMap = (Map) _nonsearchableGroupToKeyMap.get(gvo);
        if (removalMap != null) {
            removalMap.remove(key);
        }

        Map keyMap = (Map) _searchableGroupToKeyMap.get(gvo);
        if (keyMap == null) {
            keyMap = new HashMap();
            _searchableGroupToKeyMap.put(gvo, keyMap);
        }

        keyMap.put(key, value);
        _searchableGroupToKeyMap.put(gvo, keyMap);
    }

    public void loadFromObjectXml(String xml) {

        LOG.info("Started loadFromObjectXml method");

        XStream xstream = null;
        try {
            if (checkNull(xml)) {
                // Nothing to load.
                return;
            }

            // Strip error map from XML for testing
            xml = xml.replaceAll("<errorMap/>", "");

            SAXBuilder saxBuilder = new SAXBuilder();
            // ######### assign doc as null, and close the StringReader in finally method
            Document doc = saxBuilder.build(new StringReader(xml));
            Element rootElement = doc.getRootElement();

            xstream = (XStream)xstreamPool.borrowObject();
            XMLOutputter outputter = new XMLOutputter();

            // Group info needs to be in the root.
            Collection groups = rootElement.getChildren();
//            LOG.info(groups.size() + " groups found.");
            for (Iterator j = groups.iterator(); j.hasNext();) {
                Element currElement = (Element) j.next();
                String groupType = currElement.getName();
                String groupName = currElement.getAttributeValue("id");

//                LOG.info(currElement.getChildren().size() + " children for the group");
                Map searchableKeyMap = new HashMap();
                Map nonSearchKeyMap = new HashMap();
                for (Iterator i = currElement.getChildren().iterator(); i.hasNext();) {
                    Element child = (Element) i.next();
                    if (child.getAttribute("searchable") != null) {
                        searchableKeyMap.put(child.getName(), child.getText());
                    } else {
                        Element object = child;
                        if (!child.getChildren().isEmpty()) {
                            // Load XML-generated Java object from child.
                            object = (Element) child.getChildren().iterator().next();
                            if (checkNull(object.getText()) &&
                                    (object.getChildren() == null || object.getChildren().isEmpty())) {
                                // empty object - to heck with it.
                                object = child;
                            }
                        }
//                        LOG.info("Object:" + outputter.outputString(object));
                        nonSearchKeyMap.put(child.getName(), (object == child ? child.getText() : xstream.fromXML(outputter.outputString(object))));
                    }
                }

                GroupVO gvo = new GroupVO();
                gvo.setGroupType(groupType);
                gvo.setGroupName(groupName);

//                LOG.info("Storing " + nonSearchKeyMap.size() + " keys for " + groupType + "/" + groupName);
                if (!searchableKeyMap.isEmpty()) {
                    _searchableGroupToKeyMap.put(gvo, searchableKeyMap);
                }
                if (!nonSearchKeyMap.isEmpty()) {
                    _nonsearchableGroupToKeyMap.put(gvo, nonSearchKeyMap);
                }
            }

            LOG.info("Completed loadFromObjectXml method");

//            LOG.info(getFieldXml());
        } catch (Exception ex) {
            LOG.error("Unable to parse XML",ex);
            LOG.error(" IN XML:" + xml);
        } finally {
            if (xstream != null) {
                try {
                    xstreamPool.returnObject(xstream);
                } catch (Exception ex) {
                    LOG.error("error returning xstream: " + ex.getMessage(), ex);
                }
            }
        }
    }

    public static boolean checkNull(String s) {

        return (s == null || s.trim().length() == 0);
    }

    private static GenericObjectPool xstreamPool = new GenericObjectPool(new PoolableObjectFactory() {
        @Override
        public Object makeObject() throws Exception {
            XStream xstream = new XStream(new PureJavaReflectionProvider());
            xstream.setMode(XStream.NO_REFERENCES);
            return xstream;
        }

        @Override public void destroyObject(Object o) throws Exception {}
        @Override public boolean validateObject(Object o) { return true; }
        @Override public void activateObject(Object o) throws Exception {}
        @Override public void passivateObject(Object o) throws Exception { }
    });
    static {
        xstreamPool.setTestOnBorrow(false);
        xstreamPool.setTestOnReturn(false);
        xstreamPool.setMinIdle(5);
        xstreamPool.setMaxIdle(25);
        xstreamPool.setMaxActive(25);
        xstreamPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        xstreamPool.setMaxWait(1000 * 60 * 1); //1 min
        xstreamPool.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15); //15 min
    }


    public String getFieldXml() {

        LOG.info("Started getFieldXml method");

        Set nonSearchKeys = _nonsearchableGroupToKeyMap.keySet();
        Set searchKeys = _searchableGroupToKeyMap.keySet();
        String xml = "<details>";
        for (Iterator i = nonSearchKeys.iterator(); i.hasNext();) {
            GroupVO gvo = (GroupVO) i.next();
            xml += "<" + gvo.getGroupType() + " id=\"" + encodeMaliciousCharacters(gvo.getGroupName()) + "\">";

            Map nonSearchFields = (Map) (_nonsearchableGroupToKeyMap).get(gvo);
            Map searchFields = (Map) _searchableGroupToKeyMap.get(gvo);
            for (Iterator j = nonSearchFields.keySet().iterator(); j.hasNext();) {
                String key = (String) j.next();
                xml += "<" + key + ">" + encodeMaliciousCharacters(nonSearchFields.get(key) == null ? "null" : nonSearchFields.get(key).toString()) + "</" + key + ">";
            }
            if (searchFields != null) {
//                LOG.info("Loading search fields.");
                for (Iterator j = searchFields.keySet().iterator(); j.hasNext();) {
                    String key = (String) j.next();
                    xml += "<" + key + ">" + encodeMaliciousCharacters(searchFields.get(key) == null ? "null" : searchFields.get(key).toString()) + "</" + key + ">";
                }
            }
            xml += "</" + gvo.getGroupType() + ">";
        }

        for (Iterator i = searchKeys.iterator(); i.hasNext();) {
            GroupVO gvo = (GroupVO) i.next();
            if (!nonSearchKeys.contains(gvo)) {
                xml += "<" + gvo.getGroupType() + " id=\"" + encodeMaliciousCharacters(gvo.getGroupName()) + "\">";
                Map searchFields = (Map) _searchableGroupToKeyMap.get(gvo);
                for (Iterator j = searchFields.keySet().iterator(); j.hasNext();) {
                    String key = (String) j.next();
                    xml += "<" + key + ">" + encodeMaliciousCharacters(searchFields.get(key) == null ? "null" : searchFields.get(key).toString()) + "</" + key + ">";
                }
                xml += "</" + gvo.getGroupType() + ">";
            }
        }
        xml += "</details>";

        LOG.info("Completed getFieldXml method");

        return xml;
    }

    public static String encodeMaliciousCharacters(String val, boolean checkAmpAlreadyEscaped) {

        if (val == null) {
            return "";
        }

        final char repChr = ' ';
        final StringBuilder result = new StringBuilder(val.length());

        char character;

        for (int i = 0; i < val.length(); ++i) {
            character = val.charAt(i);

            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '\\') {
                result.append("&#092;");
            }
            //else if (character == '&' && ((!checkAmpAlreadyEscaped) || (!substringSafe(val, i, i + 5).equalsIgnoreCase("&amp;")))) {
            //   result.append("&amp;");
            //}
            else {
                //the char is not a special one
                //add it to the result as is
                if (character < ' '
                        && character != '\b'
                        && character != '\t'
                        && character != '\f'
                        && character != '\t'
                        && character != '\n') {
                    character = repChr;
                }
                result.append(HtmlUtils.htmlEscape(String.valueOf(character)));
                //System.out.println("Character = " + character);
            }
        }
        return result.toString();
    }

    public static String encodeMaliciousCharacters(String val) {
        return encodeMaliciousCharacters(val, true);
    }



}
