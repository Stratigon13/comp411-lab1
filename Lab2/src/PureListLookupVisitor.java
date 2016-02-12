/**
// * Created by xiaozheng on 2/11/16.
// */

public class PureListLookupVisitor<Binding, ResType> implements PureListVisitor<Binding, ResType>{
    PureListLookupVisitor(Binding v){

    }

    @Override
    public ResType forEmpty(Empty<Binding> e) {
        return null;
    }

    @Override
    public ResType forCons(Cons<Binding> c) {
        return null;
    }
}