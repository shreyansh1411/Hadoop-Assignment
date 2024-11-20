import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class two {

    public static class TempMapper extends Mapper<Object, Text, Text, IntWritable> {
        private Text year = new Text();
        private IntWritable temperature = new IntWritable();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String date = parts[0];
                int temp = Integer.parseInt(parts[2]);
                year.set(date.substring(0, 4));
                temperature.set(temp);
                context.write(year, temperature);
            }
        }
    }

    public static class TempReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int minTemp = Integer.MAX_VALUE;
            for (IntWritable val : values) {
                minTemp = Math.min(minTemp, val.get());
            }
            result.set(minTemp);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "minimum temperature");
        job.setJarByClass(two.class);
        job.setMapperClass(TempMapper.class);
        job.setCombinerClass(TempReducer.class);
        job.setReducerClass(TempReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}