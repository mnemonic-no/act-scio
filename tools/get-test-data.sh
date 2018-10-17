#!/bin/sh

ROOT=$(git rev-parse --show-toplevel)

wget https://foxitsecurity.files.wordpress.com/2016/06/fox-it_mofang_threatreport_tlp-white.pdf -O $ROOT/test_data/fox-it_mofang_threatreport_tlp-white.pdf
wget https://info.lookout.com/rs/051-ESQ-475/images/Lookout_Dark-Caracal_srr_20180118_us_v.1.0.pdf -O $ROOT/test_data/Lookout_Dark-Caracal_srr_20180118_us_v.1.0.pdf

