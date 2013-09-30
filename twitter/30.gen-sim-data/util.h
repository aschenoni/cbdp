#include <string>
#include <boost/filesystem.hpp>

#define cout_ cout << "[" << boost::filesystem::path(__FILE__).filename().string() << " " << __LINE__ << "] "


std::string cur_datetime();

std::string* read_file_raw(const char* fn);
