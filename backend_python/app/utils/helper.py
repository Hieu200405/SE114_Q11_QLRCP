from datetime import datetime, date, time



def format_date(date_obj):
    """Format a date object to a string in 'YYYY-MM-DD' format."""
    return datetime.strptime(date_obj, '%Y-%m-%d').date() if date_obj else None


def format_time(time_obj):
    """Format a time object to a string in 'HH:MM:SS' format."""
    return datetime.strptime(time_obj, '%H:%M:%S').time() if time_obj else None

def format_datetime(datetime_obj):
    """Format a datetime object to a string in 'YYYY-MM-DD HH:MM:SS' format."""
    return datetime.strptime(datetime_obj, '%Y-%m-%d %H:%M:%S') if datetime_obj else None