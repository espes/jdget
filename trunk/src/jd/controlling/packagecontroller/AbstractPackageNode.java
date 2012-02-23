package jd.controlling.packagecontroller;

import java.util.List;

public interface AbstractPackageNode<V extends AbstractPackageChildrenNode<E>, E extends AbstractPackageNode<V, E>> extends AbstractNode, AbstractNodeNotifier<V> {

    PackageController<E, V> getControlledBy();

    void setControlledBy(PackageController<E, V> controller);

    List<V> getChildren();

    ChildrenView<V> getView();

    void notifyStructureChanges();

    // void notifyPropertyChanges();

    boolean isExpanded();

    void setExpanded(boolean b);

    int indexOf(V child);

}
