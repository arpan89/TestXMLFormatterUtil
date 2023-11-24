import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AppFormatXML extends AbstractDAO {

    static Logger LOG = LoggerFactory.getLogger(AppFormatXML.class);

    private static Map _searchableGroupToKeyMap = new HashMap();

    private static Map _nonsearchableGroupToKeyMap = new HashMap();
    private GroupVO gvo;

    public AppFormatXML(String objectName) {
        super(objectName);
    }

    public static void main(String[] args) throws SQLException {

        String s = "<txn>\n" +
                "<messageid>68405679-7f01-4ac9-8bb2-ea3b6cb730ec</messageid>\n" +
                "<timestamp>2023100312:42:56</timestamp>\n" +
                "<companycode>MS</companycode>\n" +
                "<username>1</username>\n" +
                "<service>CURRENTDAYREALTIMEBR</service>\n" +
                "<subservice></subservice>\n" +
                "<channel>FAX</channel>\n" +
                "<formatpref>PDF</formatpref>\n" +
                "<resend>false</resend>\n" +
                "<resentmessageid></resentmessageid>\n" +
                "<failure>false</failure>\n" +
                "<txnid>SCHEDULEDTEMPLATETASK-20230818-200000</txnid>\n" +
                "<billingprincipalid></billingprincipalid>\n" +
                "<isnotification>false</isnotification>\n" +
                "</txn>";

        AppFormatXML appFormatXML = new AppFormatXML(s);
        String s1 = appFormatXML.processXMLString(s);

        System.out.println(s1);
    }

    public String processXMLString(String s) throws SQLException {

        String inputXML = "<details><DetailsStrings id=\"\"><asofdate ><string>2023-10-25</string></asofdate><attachments ><int>0</int></attachments><MailSubject ><string>balance report</string></MailSubject><publicTemplate ><boolean>false</boolean></publicTemplate><encryptedid ><string>7UyBmKI0xCrbl9OKKddY7e/TlaJC1DEA7+5hXq4eQ6QawyoKCMPn5m5IWBFXFDb5</string></encryptedid><navTargetUrl ><null/></navTargetUrl><imageUrl ><string>https://corporatefdlqa.qa.cc.banksol.onefiserv.net</string></imageUrl><productname ><string>Large Business</string></productname><templatename ><string>new</string></templatename><servicename ><string>Balance Reporting</string></servicename><stylename ><string>WebReport</string></stylename><registrationUrl ><string>https://corporatefdlqa.qa.cc.banksol.onefiserv.net/welcome?id=7UyBmKI0xCrbl9OKKddY7e%2FTlaJC1DEA7%2B5hXq4eQ6QawyoKCMPn5m5IWBFXFDb5</string></registrationUrl><companyCode ><string>anjali20</string></companyCode><ApplyDigitalSignature ><boolean>false</boolean></ApplyDigitalSignature><signed ><boolean>false</boolean></signed><principalid ><string>533</string></principalid><smtplog ><string></string></smtplog><templateid ><string>18113</string></templateid><pdfcode ><string>123456</string></pdfcode><bytes ><int>6185</int></bytes><fileTxnId ><string>SCHEDULEDTEMPLATETASK-20231027-013000</string></fileTxnId><bankname ><string>Fiserv</string></bankname><AttachmentMimeType ><string>application/pdf</string></AttachmentMimeType><stylecode ><string>CURRENTDAYREALTIMEBR</string></stylecode><username ><string>anjuser</string></username></DetailsStrings><ACCOUNT_GROUP id=\"413771\"><aba ><string>111111118</string></aba><accountName ><string>ss</string></accountName><originalAba ><string>111111118</string></originalAba><accountNumber ><string>111000111</string></accountNumber></ACCOUNT_GROUP></details>";
        DetailsVO dvo = loadDetails("acf1d418bb2ddbb191c35@ONB", "distribution", inputXML);
        String outS = s.substring(0, s.lastIndexOf("</txn>")) + dvo.getFieldXml() + "</txn>";

        return outS;
    }

}
