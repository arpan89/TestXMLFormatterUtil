import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public abstract class AbstractDAO {

    Logger LOG = LoggerFactory.getLogger(AbstractDAO.class);
    public String objectName;

    public AbstractDAO (String objectName) {
        this.objectName = objectName;
    }

    public DetailsVO loadDetails(String parentuid, String prefix, String staticDetails) throws SQLException {
        DetailsVO dvo = new DetailsVO();
        dvo.loadFromObjectXml(staticDetails);
        GroupVO gvo = new GroupVO();
        gvo.setGroupType("DetailsStrings");
        gvo.setGroupName("");
        dvo.addSearchableField(gvo, "smsMsgidVendor", "5913258f49d74a447b3829e6d6626f7b");
        return dvo;
    }
}
