package me.silviogames.geha;

public enum Anim
{
   // SIMPLE ANIMATION SYSTEM

   DUMMY(2, 1f, -1, false, 0, 1),
   PARTICLE_BLOOD(4, 0.07f, -1, false, 0, 0, 1, 2, 3),

   ;
   public static float[] returner = new float[3];
   public final int keyframe, num_frames; // number of individual frames, may appear more than once in anim
   public final boolean flip;
   public final int[] frames;
   public final float frame_time, total_time;

   Anim(int num_indiv_frames, float frame_time, int keyframe, boolean flip, int... frames)
   {
      this.num_frames = num_indiv_frames;
      this.flip = flip;
      this.frame_time = frame_time;
      this.total_time = frame_time * frames.length;
      this.frames = frames;
      this.keyframe = keyframe;
   }

   public float[] update(float current_time, float change)
   {
      returner[1] = 0;
      returner[2] = 0;
      // returner[0] == 0.343f -> current time after change
      // returner[1] == 1 -> keyframe
      // returner[2] == 1 -> end of animation

      // frame time is returned negative if this is keyframe!
      int old_frame = get_frame(current_time);
      float next_time = current_time + change;
      int new_frame = get_frame(next_time);
      if (new_frame != old_frame && new_frame == keyframe)
      {
         returner[1] = 1f;
      }
      if (next_time > total_time)
      {
         if (next_time > total_time * 2)
         {
            returner[0] = 0f;
         } else
         {
            returner[0] = next_time - total_time;
         }
         returner[2] = 1f;
      } else
      {
         returner[0] = next_time;
      }
      return returner;
   }

   public int get_frame(float time)
   {
      return frames[((int) (time / frame_time)) % frames.length];
   }
}