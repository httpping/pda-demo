package com.olc.util;

import android.content.Context;

import com.olc.reader.R;


public class ErrorCodeInfo {

    public static final byte command_success = 0x10;

    public static final byte command_fail = 0x11;

    public static final byte mcu_reset_error = 0x20;

    public static final byte cw_on_error = 0x21;

    public static final byte antenna_missing_error = 0x22;

    public static final byte write_flash_error = 0x23;

    public static final byte read_flash_error = 0x24;

    public static final byte set_output_power_error = 0x25;

    public static final byte tag_inventory_error = 0x31;

    public static final byte tag_read_error = 0x32;

    public static final byte tag_write_error = 0x33;

    public static final byte tag_lock_error = 0x34;

    public static final byte tag_kill_error = 0x35;

    public static final byte no_tag_error = 0x36;

    public static final byte inventory_ok_but_access_fail = 0x37;

    public static final byte buffer_is_empty_error = 0x38;

    public static final byte nxp_custom_command_fail = 0x3C;

    public static final byte access_or_password_error = 0x40;

    public static final byte parameter_invalid = 0x41;

    public static final byte parameter_invalid_wordCnt_too_long = 0x42;

    public static final byte parameter_invalid_membank_out_of_range = 0x43;

    public static final byte parameter_invalid_lock_region_out_of_range = 0x44;

    public static final byte parameter_invalid_lock_action_out_of_range = 0x45;

    public static final byte parameter_reader_address_invalid    = 0x46;

    public static final byte parameter_invalid_antenna_id_out_of_range    = 0x47;

    public static final byte parameter_invalid_output_power_out_of_range    = 0x48;

    public static final byte parameter_invalid_frequency_region_out_of_range    = 0x49;

    public static final byte parameter_invalid_baudrate_out_of_range    = 0x4A;

    public static final byte parameter_beeper_mode_out_of_range    = 0x4B;

    public static final byte parameter_epc_match_len_too_long    = 0x4C;

    public static final byte parameter_epc_match_len_error    = 0x4D;

    public static final byte parameter_invalid_epc_match_mode    = 0x4E;

    public static final byte parameter_invalid_frequency_range    = 0x4F;

    public static final byte fail_to_get_RN16_from_tag    = 0x50;

    public static final byte parameter_invalid_drm_mode    = 0x51;

    public static final byte pll_lock_fail    = 0x52;

    public static final byte rf_chip_fail_to_response     = 0x53;

    public static final byte fail_to_achieve_desired_output_power     = 0x54;

    public static final byte copyright_authentication_fail     = 0x55;

    public static final byte spectrum_regulation_error     = 0x56;

    public static final byte output_power_too_low     = 0x57;

    public static final byte fail_to_get_rf_port_return_loss     = (byte)0xEE;

    static String getStr(Context context, int id){
        return context.getResources().getString(id);
    }

    public static String getErrorInfo(Context context, byte ErrorCode){
        String info = "";
        switch (ErrorCode){
            case command_success:
                info = getStr(context, R.string.command_success);
                break;
            case command_fail:
                info = getStr(context, R.string.command_fail);
                break;
            case mcu_reset_error:
                info = getStr(context, R.string.mcu_reset_error);
                break;
            case cw_on_error:
                info = getStr(context, R.string.cw_on_error);
                break;
            case antenna_missing_error:
                info = getStr(context, R.string.antenna_missing_error);
                break;
            case write_flash_error:
                info = getStr(context, R.string.write_flash_error);
                break;
            case read_flash_error:
                info = getStr(context, R.string.read_flash_error);
                break;
            case set_output_power_error:
                info = getStr(context, R.string.set_output_power_error);
                break;
            case tag_inventory_error:
                info = getStr(context, R.string.tag_inventory_error);
                break;
            case tag_read_error:
                info = getStr(context, R.string.tag_read_error);
                break;
            case tag_write_error:
                info = getStr(context, R.string.tag_write_error);
                break;
            case tag_lock_error:
                info = getStr(context, R.string.tag_lock_error);
                break;
            case tag_kill_error:
                info = getStr(context, R.string.tag_kill_error);
                break;
            case no_tag_error:
                info = getStr(context, R.string.no_tag_error);
                break;
            case inventory_ok_but_access_fail:
                info = getStr(context, R.string.inventory_ok_but_access_fail);
                break;
            case buffer_is_empty_error:
                info = getStr(context, R.string.buffer_is_empty_error);
                break;
            case nxp_custom_command_fail:
                info = getStr(context, R.string.nxp_custom_command_fail);
                break;
            case access_or_password_error:
                info = getStr(context, R.string.access_or_password_error);
                break;
            case parameter_invalid:
                info = getStr(context, R.string.parameter_invalid);
                break;
            case parameter_invalid_wordCnt_too_long:
                info = getStr(context, R.string.parameter_invalid_wordCnt_too_long);
                break;
            case parameter_invalid_membank_out_of_range:
                info = getStr(context, R.string.parameter_invalid_membank_out_of_range);
                break;
            case parameter_invalid_lock_region_out_of_range:
                info = getStr(context, R.string.parameter_invalid_lock_region_out_of_range);
                break;
            case parameter_invalid_lock_action_out_of_range:
                info = getStr(context, R.string.parameter_invalid_lock_action_out_of_range);
                break;
            case parameter_reader_address_invalid:
                info = getStr(context, R.string.parameter_reader_address_invalid);
                break;
            case parameter_invalid_antenna_id_out_of_range:
                info = getStr(context, R.string.parameter_invalid_antenna_id_out_of_range);
                break;
            case parameter_invalid_output_power_out_of_range:
                info = getStr(context, R.string.parameter_invalid_output_power_out_of_range);
                break;
            case parameter_invalid_frequency_region_out_of_range:
                info = getStr(context, R.string.parameter_invalid_frequency_region_out_of_range);
                break;
            case parameter_invalid_baudrate_out_of_range:
                info = getStr(context, R.string.parameter_invalid_baudrate_out_of_range);
                break;
            case parameter_beeper_mode_out_of_range:
                info = getStr(context, R.string.parameter_beeper_mode_out_of_range);
                break;
            case parameter_epc_match_len_too_long:
                info = getStr(context, R.string.parameter_epc_match_len_too_long);
                break;
            case parameter_epc_match_len_error:
                info = getStr(context, R.string.parameter_epc_match_len_error);
                break;
            case parameter_invalid_epc_match_mode:
                info = getStr(context, R.string.parameter_invalid_epc_match_mode);
                break;
            case parameter_invalid_frequency_range:
                info = getStr(context, R.string.parameter_invalid_frequency_range);
                break;
            case fail_to_get_RN16_from_tag:
                info = getStr(context, R.string.fail_to_get_RN16_from_tag);
                break;
            case parameter_invalid_drm_mode:
                info = getStr(context, R.string.parameter_invalid_drm_mode);
                break;
            case pll_lock_fail:
                info = getStr(context, R.string.pll_lock_fail);
                break;
            case rf_chip_fail_to_response:
                info = getStr(context, R.string.rf_chip_fail_to_response);
                break;
            case fail_to_achieve_desired_output_power:
                info = getStr(context, R.string.fail_to_achieve_desired_output_power);
                break;
            case copyright_authentication_fail:
                info = getStr(context, R.string.copyright_authentication_fail);
                break;
            case spectrum_regulation_error:
                info = getStr(context, R.string.spectrum_regulation_error);
                break;
            case output_power_too_low:
                info = getStr(context, R.string.output_power_too_low);
                break;
            case fail_to_get_rf_port_return_loss:
                info = getStr(context, R.string.fail_to_get_rf_port_return_loss);
                break;

        }
        return info;
    }
}
