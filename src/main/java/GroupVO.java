import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GroupVO implements Serializable {

    public Map errorMap;

    private String _groupType;
    private String _groupName = "";

    public GroupVO() {
        errorMap = new HashMap();
    }

    public GroupVO(String groupType, String groupName) {
        setGroupType(groupType);
        setGroupName(groupName);
    }

    public String getGroupType() {
        return _groupType;
    }

    public void setGroupType(String _groupType) {
        this._groupType = _groupType;
    }

    public String getGroupName() {
        return _groupName;
    }

    public void setGroupName(String _groupName) {
        this._groupName = _groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupVO groupVO = (GroupVO) o;
        return _groupType.equals(groupVO._groupType) && _groupName.equals(groupVO._groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_groupType, _groupName);
    }

    public int compareTo(Object o) {
        if (o instanceof GroupVO) {
            GroupVO gvo = (GroupVO) o;
            return getGroupType() == null ? null :
                    (getGroupType().compareTo(gvo.getGroupType()) == 0 ?
                            (getGroupName() == null ? 0 : getGroupName().compareTo(gvo.getGroupName())) :
                            (getGroupType().compareTo(gvo.getGroupType())));
        } else {
            return 0;
        }
    }
}
