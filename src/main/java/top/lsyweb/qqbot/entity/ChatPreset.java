package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("tbl_chat_preset")
public class ChatPreset {
  @TableId
  private Integer id;
  private Integer presetId;
  @TableField(value = "`desc`")
  private String desc;
  @TableField(value = "`status`")
  private Integer status;
  private String messages;
  private String temperature;
  private Integer maxTokens;
  private Integer memorySize;

  public ChatPreset(ChatPreset chatPreset) {
    this.id = chatPreset.id;
    this.presetId = chatPreset.presetId;
    this.desc = chatPreset.desc;
    this.status = chatPreset.status;
    this.messages = chatPreset.messages;
    this.temperature = chatPreset.temperature;
    this.maxTokens = chatPreset.maxTokens;
    this.memorySize = chatPreset.memorySize;
  }
}
