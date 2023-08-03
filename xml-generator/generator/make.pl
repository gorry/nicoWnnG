#!/usr/bin/perl
# ◇ xmlファイル群生成スクリプト

unlink <../*.xml>;

# base keyboard
while (<base/*.xml>) {
	$srcfile = $_;
	print "$srcfile\n";

	for ($i=0; $i<1; $i++ )
	{
		# ==================================================================
		$dstfile = $srcfile;
		$dstfile =~ s:base/(.*)([.]xml)$:../$1_${i}$2:;
		# print "  $dstfile\n";

		open(FIN, $srcfile);
		open(FOUT, ">$dstfile");
		while(<FIN>) {
			s:key_mid_height:key_height_${i}:;
			s:keyboard_popup_qwerty_jp:keyboard_popup_qwerty_jp_${i}:;
			s:keyboard_popup_12key_jp:keyboard_popup_12key_jp_${i}:;
			s:keyboard_popup_nico2_jp:keyboard_popup_nico2_jp_${i}:;
			s:keyboard_popup_subten_qwerty_jp:keyboard_popup_subten_qwerty_jp_${i}:;
			s:keyboard_popup_subten_12key_jp:keyboard_popup_subten_12key_jp_${i}:;
			print FOUT;
		}
		close(FIN);
		close(FOUT);

	}
}

# base2 keyboard (tile)
while (<base2/*.xml>) {
	$srcfile = $_;
	print "$srcfile\n";

	for ($i=0; $i<1; $i++ )
	{
		# ==================================================================
		$dstfile = $srcfile;
		$dstfile =~ s:base2/(.*)([.]xml)$:../$1_${i}$2:;
		# print "  $dstfile\n";

		open(FIN, $srcfile);
		open(FOUT, ">$dstfile");
		while(<FIN>) {
			s:key_mid_height:key_height_${i}:;
			s:keyboard_popup_qwerty_jp:keyboard_popup_qwerty_jp_${i}:;
			s:keyboard_popup_12key_jp:keyboard_popup_12key_jp_${i}:;
			s:keyboard_popup_nico2_jp:keyboard_popup_nico2_jp_${i}:;
			s:keyboard_popup_subten_qwerty_jp:keyboard_popup_subten_qwerty_jp_${i}:;
			s:keyboard_popup_subten_12key_jp:keyboard_popup_subten_12key_jp_${i}:;
			s:%key_width%:7%p:;
			s:%key_width_1%:14.10%p:;
			s:%key_width_2%:10%p:;
			s:%key_width_3%:7.90%p:;
			s:%key_width_l1%:10.00%p:;
			s:%key_width_l2%:10.00%p:;
			s:%key_width_l3%:10.00%p:;
			s:%key_width_l4%:10.00%p:;
			s:%key_width_l5%:10.00%p:;
			s:%key_gap_1%:1%p:;
			s:%key_gap_2%:1%p:;
			s:%key_gap_3%:1%p:;
			s:%key_gap_4%:1%p:;
			s:%key_gap_5%:1%p:;
			s:%key_gap_6%:0%p:;
			s:%key_gap_7%:0%p:;
			s:%key_gap_8%:0%p:;
			s:%key_gap_9%:0%p:;
			next if ( s/local:option="1"// );
			s/local:option="2"/android:keyEdgeFlags="right"/;
			next if ( s/local:option="3"// );
			next if ( s/local:option="4"// );
			s/local:option="5"//;
			print FOUT;
		}
		close(FIN);
		close(FOUT);

	}
}


# base2 keyboard (slant)
while (<base2/*.xml>) {
	$srcfile = $_;
	print "$srcfile\n";

	for ($i=0; $i<1; $i++ )
	{
		# ==================================================================
		$dstfile = $srcfile;
		$dstfile =~ s:base2/(.*)([.]xml)$:../$1_s${i}$2:;
		# print "  $dstfile\n";

		open(FIN, $srcfile);
		open(FOUT, ">$dstfile");
		while(<FIN>) {
			s:key_mid_height:key_height_${i}:;
			s:keyboard_popup_qwerty_jp:keyboard_popup_qwerty_jp_${i}:;
			s:keyboard_popup_12key_jp:keyboard_popup_12key_jp_${i}:;
			s:keyboard_popup_nico2_jp:keyboard_popup_nico2_jp_${i}:;
			s:keyboard_popup_subten_qwerty_jp:keyboard_popup_subten_qwerty_jp_${i}:;
			s:keyboard_popup_subten_12key_jp:keyboard_popup_subten_12key_jp_${i}:;
			s:%key_width%:6.5%p:;
			s:%key_width_1%:13%p:;
			s:%key_width_2%:9%p:;
			s:%key_width_3%:6.90%p:;
			s:%key_width_l1%:10.00%p:;
			s:%key_width_l2%:10.00%p:;
			s:%key_width_l3%:6.7%p:;
			s:%key_width_l4%:13.50%p:;
			s:%key_width_l5%:6.7%p:;
			s:%key_gap_1%:0%p:;
			s:%key_gap_2%:3%p:;
			s:%key_gap_3%:5%p:;
			s:%key_gap_4%:8%p:;
			s:%key_gap_5%:1%p:;
			s:%key_gap_6%:4%p:;
			s:%key_gap_7%:8%p:;
			s:%key_gap_8%:3.5%p:;
			s:%key_gap_9%:3.45%p:;
			s/local:option="1"//;
			s/local:option="2"//;
			s/local:option="3"/android:keyEdgeFlags="right"/;
			s/local:option="4"//;
			next if ( s/local:option="5"// );
			print FOUT;
		}
		close(FIN);
		close(FOUT);

	}
}

# base3 keyboard (popup)
while (<base3/*.xml>) {
	$srcfile = $_;
	print "$srcfile\n";

	for ($i=0; $i<10; $i++ )
	{
		# ==================================================================
		$dstfile = $srcfile;
		$dstfile =~ s:base3/(.*)([.]xml)$:../$1_${i}$2:;
		# print "  $dstfile\n";

		open(FIN, $srcfile);
		open(FOUT, ">$dstfile");
		while(<FIN>) {
			s:popup_12key_height:popup_12key_height_${i}:;
			s:popup_nico2_height:popup_nico2_height_${i}:;
			s:popup_qwerty_height:popup_qwerty_height_${i}:;
			s:popup_subten_qwerty_height:popup_subten_qwerty_height_${i}:;
			s:popup_subten_12key_height:popup_subten_12key_height_${i}:;
			print FOUT;
		}
		close(FIN);
		close(FOUT);

	}
}

while (<etc/*.xml>) {
	$srcfile = $_;
	print "$srcfile\n";

	$dstfile = $srcfile;
	$dstfile =~ s:etc/:../:;
	# print "  $dstfile\n";

	open(FIN, $srcfile);
	open(FOUT, ">$dstfile");
	while(<FIN>) {
		print FOUT;
	}
	close(FIN);
	close(FOUT);
}
