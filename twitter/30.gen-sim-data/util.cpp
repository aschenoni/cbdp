#include <sys/time.h>
#include <fstream>
#include <boost/format.hpp>

#include "util.h"

using namespace std;


std::string cur_datetime()
{
	struct timeval tv;
	gettimeofday(&tv, NULL);
	char str[20];
	strftime(str, sizeof(str), "%y-%m-%d %T", localtime(&tv.tv_sec));
	return string(str);
}


string* read_file_raw(const char* fn) {
	ifstream ifs(fn, std::ios::in | std::ios::binary);
  if (! ifs.is_open())
    throw runtime_error(str(boost::format("unable to open file %1%") % fn));
	ifs.seekg(0, ios::end);   
	string* data = new string();
	data->reserve(ifs.tellg());
	ifs.seekg(0, ios::beg);
	data->assign((istreambuf_iterator<char>(ifs)), istreambuf_iterator<char>());
	return data;
}
