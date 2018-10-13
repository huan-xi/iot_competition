package cn.huse.prepare.adapter;

public enum ContentPage {
    Item1(0),
    Item2(1);
    public static final int SIZE = 2;
    private final int position;

    ContentPage(int pos) {
        position = pos;
    }

    public static ContentPage getPage(int position) {
        switch (position) {
            case 0:
                return Item1;
            case 1:
                return Item2;
            default:
                return Item1;
        }
    }

    public int getPosition() {
        return position;
    }
}
