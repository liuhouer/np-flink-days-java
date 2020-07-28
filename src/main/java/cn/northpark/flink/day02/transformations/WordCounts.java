package cn.northpark.flink.day02.transformations;

//封装数据的Bean
public class WordCounts {

    public String word;

    public Long counts;

    public WordCounts() {}

    public WordCounts(String word, Long counts) {
        this.word = word;
        this.counts = counts;
    }
//
//    public String getWord() {
//        return word;
//    }
//
//    public void setWord(String word) {
//        this.word = word;
//    }
//
//    public Long getCounts() {
//        return counts;
//    }
//
//    public void setCounts(Long counts) {
//        this.counts = counts;
//    }


    public static WordCounts of(String word, Long counts) {
        return new WordCounts(word, counts);
    }

    @Override
    public String toString() {
        return "WordCounts{" + "word='" + word + '\'' + ", counts=" + counts + '}';
    }
}
